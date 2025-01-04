package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.onyx.darie.calin.gentleglowonyxboox.light.Brightness;
import com.onyx.darie.calin.gentleglowonyxboox.light.BrightnessAndWarmth;
import com.onyx.darie.calin.gentleglowonyxboox.light.BrightnessAndWarmthState;
import com.onyx.darie.calin.gentleglowonyxboox.light.Light;
import com.onyx.darie.calin.gentleglowonyxboox.light.Warmth;
import com.onyx.darie.calin.gentleglowonyxboox.setup.GentleGlowApplication;

public class ChangeLightReceiver extends BroadcastReceiver {
    public static final String ACTION_CHANGE_LIGHT = "com.onyx.darie.calin.gentleglowonyxboox.CHANGE_LIGHT";
    public static final String EXTRA_BRIGHTNESS = "BRIGHTNESS";
    public static final String EXTRA_WARMTH = "WARMTH";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_CHANGE_LIGHT.equals(intent.getAction()))  return;

        Light light  = ((GentleGlowApplication) context.getApplicationContext()).getDependencies().getLight();
        BrightnessAndWarmthState currentState = light.getBrightnessAndWarmthState$().blockingFirst();

        int brightnessValue = intent.getIntExtra(EXTRA_BRIGHTNESS, -1);

        if (brightnessValue == 0) {
            light.turnOff();
            return;
        }

        Brightness brightness = brightnessValue < 0 || brightnessValue > 100 ?
                currentState.brightnessAndWarmth.brightness :
                new Brightness(brightnessValue);

        int warmthValue = intent.getIntExtra(EXTRA_WARMTH, -1);
        Warmth warmth = warmthValue < 0 || warmthValue > 100 ?
                currentState.brightnessAndWarmth.warmth :
                new Warmth(warmthValue);

        light.turnOn();
        light.getBrightnessAndWarmthState$()
                .skip(1)
                .firstOrError().subscribe();
        light.getSetBrightnessAndWarmthRequest$().onNext(
                new BrightnessAndWarmth(brightness, warmth));

    }
}
