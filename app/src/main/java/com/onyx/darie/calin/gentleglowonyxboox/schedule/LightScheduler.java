package com.onyx.darie.calin.gentleglowonyxboox.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditor;
import com.onyx.darie.calin.gentleglowonyxboox.setup.GentleGlowApplication;
import com.onyx.darie.calin.gentleglowonyxboox.storage.Storage;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

import io.reactivex.rxjava3.disposables.Disposable;

public class LightScheduler{
    public static final String ScheduleEntryIntentKey = "scheduleEntry";

    public void add(ScheduleEntry scheduleEntry) {
        addAlarm(scheduleEntry);
        addToStorage(scheduleEntry);
    }

    public void remove(LocalTime time) {
        removeAlarm(time);
        removeFromStorage(time);
    }

    private void addToStorage(ScheduleEntry scheduleEntry) {
        Schedule schedule = storage.loadOrDefault(new Schedule(new ArrayList<ScheduleEntry>())).value;
        schedule.entries.add(scheduleEntry);
        storage.save(schedule);
    }

    private void removeFromStorage(LocalTime time) {
        Schedule schedule = storage.loadOrDefault(new Schedule(new ArrayList<ScheduleEntry>())).value;
        schedule.entries.removeIf(scheduleEntry -> scheduleEntry.timeOfDay.equals(time));
        storage.save(schedule);
    }

    private void removeAlarm( LocalTime time) {
        Schedule schedule = storage.loadOrDefault(new Schedule(new ArrayList<ScheduleEntry>())).value;
        Optional<ScheduleEntry> entryOptional = schedule.entries.stream().filter(scheduleEntry -> scheduleEntry.timeOfDay.equals(time)).findFirst();
        if (entryOptional.isPresent()) {
            PendingIntent pendingIntent = getPendingIntent(entryOptional.get());
            alarmManager.cancel(pendingIntent);
        }
    }

    private void addAlarm(ScheduleEntry scheduleEntry) {
        PendingIntent pendingIntent = getPendingIntent(scheduleEntry);

        ZonedDateTime nextOccurrenceOfTime = getNextOccurrenceOfTime(scheduleEntry.timeOfDay);
        alarmManager.setInexactRepeating(
                AlarmManager.RTC,
                nextOccurrenceOfTime.toEpochSecond() * 1000,
                AlarmManager.INTERVAL_DAY,
                pendingIntent);
    }

    private ZonedDateTime getNextOccurrenceOfTime(LocalTime timeOfDay) {
        ZonedDateTime now = ZonedDateTime.now (ZoneId.systemDefault() );
        ZonedDateTime desiredTimeForToday = now
                .withHour ( timeOfDay.getHour() )
                .withMinute ( timeOfDay.getMinute() )
                .withSecond ( 0 )
                .withNano ( 0 );
        ZonedDateTime nextOccurrenceOfTime = desiredTimeForToday.isBefore(now) ? desiredTimeForToday.plusDays(1)
                : desiredTimeForToday;
        return nextOccurrenceOfTime;
    }

    public void handleAlarm(Intent intent) {
        String scheduledEntryAsJson = intent.getStringExtra(ScheduleEntryIntentKey);

        ScheduleEntry scheduleEntry = json.fromJson(scheduledEntryAsJson, ScheduleEntry.class);

        Disposable subscription = configurationEditor.getLightConfigurationChoices$().subscribe();
        try {
            configurationEditor.getChooseCurrentLightConfigurationRequest$()
                    .onNext(scheduleEntry.scheduledLightState.LightConfigurationIndexFallback); // todo try name first
        } finally {
            subscription.dispose();
        }
    }

    private PendingIntent getPendingIntent(ScheduleEntry scheduleEntry) {
        return getPendingIntent(intent -> {
            intent.putExtra(ScheduleEntryIntentKey, json.toJson(scheduleEntry));
            return null;
        });
    }

    protected PendingIntent getPendingIntent(Function<Intent, Void> intentConfig) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intentConfig.apply(intent);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public LightScheduler(
            Context context,
            Storage<Schedule> storage,
            LightConfigurationEditor configurationEditor
    ) {
        this.context = context;
        this.storage = storage;
        this.configurationEditor = configurationEditor;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
    private final Gson json = new Gson();
    private final Context context;
    private final Storage<Schedule> storage;
    private final LightConfigurationEditor configurationEditor;
    private final AlarmManager alarmManager;

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LightScheduler scheduler = ((GentleGlowApplication) context.getApplicationContext()).getScheduleDependencies().getLightScheduler();

            scheduler.handleAlarm(intent);
        }
    }

    public class Schedule {
        public final ArrayList<ScheduleEntry> entries;

        public Schedule(ArrayList<ScheduleEntry> entries) {
            this.entries = entries;
        }
    }
}