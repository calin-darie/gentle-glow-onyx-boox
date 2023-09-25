package com.onyx.darie.calin.gentleglowonyxboox.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightConfigurationEditor;
import com.onyx.darie.calin.gentleglowonyxboox.setup.GentleGlowApplication;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Function;

import io.reactivex.rxjava3.disposables.Disposable;

public abstract class LightScheduler
        extends BroadcastReceiver{
    private final Gson json = new Gson();
    private static final String ScheduleEntryIntentKey = "scheduleEntry";

    public void restoreAllJobs() {
    }

    public void add(Context context, ScheduleEntry scheduleEntry) {
        PendingIntent pendingIntent = getPendingIntent(context, intent -> {
            intent.putExtra(ScheduleEntryIntentKey, json.toJson(scheduleEntry));
            return null;
        });

        ZonedDateTime nextOccurrenceOfTime = getNextOccurrenceOfTime(scheduleEntry.timeOfDay);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

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



    @Override
    public void onReceive(Context context, Intent intent) {
        String scheduledEntryAsJson = intent.getStringExtra(ScheduleEntryIntentKey);

        ScheduleEntry scheduleEntry = json.fromJson(scheduledEntryAsJson, ScheduleEntry.class);
        LightConfigurationEditor configurationEditor = ((GentleGlowApplication) context.getApplicationContext())
                .getDependencies().getLightConfigurationEditor();

        Disposable subscription = configurationEditor.getLightConfigurationChoices$().subscribe();
        try {
            configurationEditor.getChooseCurrentLightConfigurationRequest$()
                    .onNext(scheduleEntry.scheduledLightState.LightConfigurationIndexFallback); // todo try name first
        } finally {
            subscription.dispose();
        }
    }

    protected abstract PendingIntent getPendingIntent(Context context, Function<Intent, Void> intentConfig);
}

