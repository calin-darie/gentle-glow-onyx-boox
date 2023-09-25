package com.onyx.darie.calin.gentleglowonyxboox.schedule;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class LightSchedulerImpl extends LightScheduler {
    @Override
    @Nullable
    protected PendingIntent getPendingIntent(Context context, Function<Intent, Void> intentConfig) {
        Intent intent = new Intent(context, LightSchedulerImpl.class);
        intentConfig.apply(intent);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
}
