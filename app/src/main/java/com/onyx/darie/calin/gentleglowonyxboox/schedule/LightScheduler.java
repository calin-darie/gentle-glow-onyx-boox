package com.onyx.darie.calin.gentleglowonyxboox.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.onyx.darie.calin.gentleglowonyxboox.light.Light;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfiguration;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditor;
import com.onyx.darie.calin.gentleglowonyxboox.setup.GentleGlowApplication;
import com.onyx.darie.calin.gentleglowonyxboox.storage.Storage;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

public class LightScheduler {
    public static final String ScheduleEntryIntentKey = "scheduleEntry";

    public Schedule defaultScheduleFromSunsetTime(LocalTime sunsetTime) {
        LocalTime midnight = LocalTime.parse("00:30");
        LocalTime nightStartTime = sunsetTime.minusMinutes(50);
        LocalTime sunriseTime = midnight.plusMinutes(sunsetTime.until(midnight, ChronoUnit.MINUTES));
        LocalTime dawnStartTime = sunriseTime.minusMinutes(50);

        ArrayList<ScheduleEntry> entries = new ArrayList<>();
        entries.add(
                new ScheduleEntry(
                        nightStartTime,
                        ScheduledLightState.onWithConfiguration("Night", 0)
                )
        );
        entries.add(
                new ScheduleEntry(
                        dawnStartTime,
                        ScheduledLightState.onWithConfiguration("Dawn", 2)
                )
        );
        entries.add(
                new ScheduleEntry(
                        sunriseTime.plusHours(1),
                        ScheduledLightState.off()
                )
        );
        return new Schedule(entries, false);
    }

    public void turnOn() {
        Schedule oldSchedule = getSchedule();
        Schedule newSchedule = new Schedule(oldSchedule.entries, true);
        storage.save(newSchedule);

        if (!oldSchedule.isOn) {
            restoreAllAlarms(newSchedule);
        }
        restoreScheduledLight(newSchedule);
    }

    private void scheduleAlarmRefresh() {
        Intent intent = new Intent(context, AlarmRefresher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setWindow(
                AlarmManager.RTC,
                getNextOccurrenceOfTime(LocalTime.parse("04:00"), ZonedDateTime.now())
                        .toEpochSecond() * 1000,
                worstCaseAndroidAlarmDelayMinutes * 60 * 1000,
                pendingIntent);
        alarmManager.setWindow(
                AlarmManager.RTC,
                getNextOccurrenceOfTime(LocalTime.parse("16:00"), ZonedDateTime.now())
                        .toEpochSecond() * 1000,
                worstCaseAndroidAlarmDelayMinutes * 60 * 1000,
                pendingIntent);
    }

    public void turnOff() {
        Schedule schedule = getSchedule();
        storage.save(new Schedule(schedule.entries, false));

        for (ScheduleEntry entry : schedule.entries) {
            removeAlarm(entry);
        }
    }

    private void restoreScheduledLight(Schedule schedule) {
        if (!schedule.isOn) return;
        if (schedule.entries.isEmpty()) return;

        Optional<ScheduleEntry> currentEntry = schedule.entries.stream()
                .filter(e -> e.timeOfDay.compareTo(LocalTime.now()) <= 0)
                .max(scheduleEntryComparatorByTime);

        ScheduleEntry entryToApply = currentEntry.isPresent() ?
                currentEntry.get() :
                schedule.entries.get(schedule.entries.size() - 1);

        apply(entryToApply);
    }

    public boolean add(ScheduleEntry scheduleEntry) {
        Schedule schedule = getSchedule();
        Optional<ScheduleEntry> entryOptional = schedule.entries.stream().filter(existingEntry -> existingEntry.timeOfDay.equals(scheduleEntry.timeOfDay)).findFirst();
        if (entryOptional.isPresent())
            return false;
        addAlarm(scheduleEntry, ZonedDateTime.now(ZoneId.systemDefault()));
        addToStorage(scheduleEntry);
        return true;
    }

    public void remove(LocalTime time) {
        Schedule schedule = getSchedule();
        Optional<ScheduleEntry> entryOptional = schedule.entries.stream().filter(scheduleEntry -> scheduleEntry.timeOfDay.equals(time)).findFirst();
        if (entryOptional.isPresent()) {
            removeFromStorage(schedule, entryOptional.get());
        }
    }

    public void replace(LocalTime oldTimeOfDay, ScheduleEntry editedEntry) {
        remove(oldTimeOfDay);
        add(editedEntry);
    }

    private void addToStorage(ScheduleEntry scheduleEntry) {
        Schedule schedule = getSchedule();
        schedule.entries.add(scheduleEntry);
        schedule.entries.sort(Comparator.comparing(entry -> entry.timeOfDay));
        storage.save(schedule);
    }

    public Schedule getSchedule() {
        Schedule defaultSchedule = defaultScheduleFromSunsetTime(LocalTime.parse("18:30"));
        return storage.loadOrDefault(defaultSchedule).value;
    }

    private void removeFromStorage(Schedule schedule, ScheduleEntry entry) {
        schedule.entries.remove(entry);
        storage.save(schedule);
    }

    private void removeAlarm(ScheduleEntry entry) {
        PendingIntent pendingIntent = getPendingIntent(entry);
        alarmManager.cancel(pendingIntent);
    }

    private void addAlarm(ScheduleEntry scheduleEntry, ZonedDateTime referenceTime) {
        addRedundantAlarm(scheduleEntry, referenceTime);
        addRedundantAlarm(new ScheduleEntry(scheduleEntry.timeOfDay.plusMinutes(worstCaseAndroidAlarmDelayMinutes),
                scheduleEntry.scheduledLightState), referenceTime);
    }

    private void addRedundantAlarm(ScheduleEntry scheduleEntry, ZonedDateTime referenceTime) {
        PendingIntent pendingIntent = getPendingIntent(scheduleEntry);

        ZonedDateTime nextOccurrenceOfTime = getNextOccurrenceOfTime(
                    scheduleEntry.timeOfDay,
                referenceTime)
                .minusMinutes(worstCaseAndroidAlarmDelayMinutes);
        alarmManager.setWindow(
                AlarmManager.RTC,
                nextOccurrenceOfTime.toEpochSecond() * 1000,
                worstCaseAndroidAlarmDelayMinutes * 60 * 1000,
                pendingIntent);
    }

    private ZonedDateTime getNextOccurrenceOfTime(LocalTime timeOfDay, ZonedDateTime referenceTime) {
        ZonedDateTime desiredTimeForToday = referenceTime
                .withHour(timeOfDay.getHour())
                .withMinute(timeOfDay.getMinute())
                .withSecond(0)
                .withNano(0);
        ZonedDateTime nextOccurrenceOfTime = desiredTimeForToday.isBefore(referenceTime) ?
                desiredTimeForToday.plusDays(1)
                : desiredTimeForToday;
        return nextOccurrenceOfTime;
    }

    public boolean isOn() {
        Schedule schedule = getSchedule();
        return schedule.isOn;
    }

    Comparator<ScheduleEntry> scheduleEntryComparatorByTime = (s1, s2) ->
            (int) ((s1.timeOfDay.toNanoOfDay() - s2.timeOfDay.toNanoOfDay()) / 1000000000);

    public void handleAlarm(LocalTime timeOfDay) {
        Schedule schedule = getSchedule();
        if (!schedule.isOn) return;
        if (schedule.entries.isEmpty()) return;

        if (!LocalTime.now().isBefore(timeOfDay) && timeOfDay.until(LocalTime.now(), ChronoUnit.HOURS) < 22) {
            restoreScheduledLight(schedule);

            return;
        }

        final long secondsUntilSwitch = LocalTime.now().isBefore(timeOfDay) ?
                LocalTime.now().until(timeOfDay, ChronoUnit.SECONDS) :
                (long) 24 * 3600 + LocalTime.now().until(timeOfDay, ChronoUnit.SECONDS);

        if (secondsUntilSwitch > (worstCaseAndroidAlarmDelayMinutes + 1) * 60)
            return;

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable restore = () -> {
            restoreScheduledLight(getSchedule());
        };
        handler.postDelayed(restore, (secondsUntilSwitch + 1) * 1000);
    }

    private void apply(ScheduleEntry scheduleEntry) {
        if (!scheduleEntry.scheduledLightState.isOn) {
            light.turnOff();
            return;
        }

        Disposable lightSubscription = light.getBrightnessAndWarmthState$().subscribe();
        Disposable lightIsOnSubscription = light.isOn$().subscribe();
        light.turnOn();
        AtomicReference<Disposable> subscriptionRef = new AtomicReference<>();
        subscriptionRef.set(configurationEditor.getLightConfigurationChoices$().subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(choice -> {
                    if (choice.getSelectedIndex() == scheduleEntry.scheduledLightState.LightConfigurationIndexFallback) {
                        subscriptionRef.get().dispose();
                        lightSubscription.dispose();
                        lightIsOnSubscription.dispose();
                    } else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> {

                            LightConfiguration[] choices = configurationEditor.getLightConfigurationChoice().getChoices();
                            int index = IntStream.range(0, choices.length)
                                    .filter(i -> choices[i].name == scheduleEntry.scheduledLightState.lightConfigurationNameMain)
                                    .findFirst().orElse(scheduleEntry.scheduledLightState.LightConfigurationIndexFallback);

                            configurationEditor.getChooseCurrentLightConfigurationRequest$()
                                    .onNext(index);
                        });
                    }
                }));
    }

    private PendingIntent getPendingIntent(ScheduleEntry scheduleEntry) {
        return getPendingIntent(intent -> {
            intent.putExtra(ScheduleEntryIntentKey, json.toJson(scheduleEntry));
        }, scheduleEntry.timeOfDay.toSecondOfDay());
    }

    protected PendingIntent getPendingIntent(Consumer<Intent> intentConfig, int id) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intentConfig.accept(intent);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public LightScheduler(
            Context context,
            Storage<Schedule> storage,
            LightConfigurationEditor configurationEditor,
            Light light
    ) {
        this.context = context;
        this.storage = storage;
        this.configurationEditor = configurationEditor;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.light = light;
    }

    private final int worstCaseAndroidAlarmDelayMinutes = 10;

    private final Gson json = new Gson();
    private final Context context;
    private final Storage<Schedule> storage;
    private final LightConfigurationEditor configurationEditor;
    private final AlarmManager alarmManager;

    private final Light light;

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LightScheduler scheduler = ((GentleGlowApplication) context.getApplicationContext()).getScheduleDependencies().getLightScheduler();
            String alarmEntryAsJson = intent.getStringExtra(ScheduleEntryIntentKey);
            Gson json = new Gson();
            ScheduleEntry alarmEntry = json.fromJson(alarmEntryAsJson, ScheduleEntry.class);

            scheduler.handleAlarm(alarmEntry.timeOfDay);
        }
    }

    public static class AlarmRefresher extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LightScheduler scheduler = ((GentleGlowApplication) context.getApplicationContext()).getScheduleDependencies().getLightScheduler();
            scheduler.restoreAllAlarms();
        }
    }

    public static class RestoreReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LightScheduler scheduler = ((GentleGlowApplication) context.getApplicationContext()).getScheduleDependencies().getLightScheduler();
            scheduler.restore();
        }
    }

    private void restore() {
        Schedule schedule = getSchedule();
        restoreAllAlarms(schedule);
        restoreScheduledLight(schedule);
    }

    public void restoreAllAlarms() {
        Schedule schedule = getSchedule();
        restoreAllAlarms(schedule);
    }

    private void restoreAllAlarms(Schedule schedule) {
        for (ScheduleEntry entry : schedule.entries) {
            addAlarm(entry, ZonedDateTime.now(ZoneId.systemDefault()));
        }
        scheduleAlarmRefresh();
    }

    public class Schedule {
        public final ArrayList<ScheduleEntry> entries;
        public final boolean isOn;

        public Schedule(ArrayList<ScheduleEntry> entries, boolean isOn) {
            this.entries = entries;
            this.isOn = isOn;
        }
    }
}