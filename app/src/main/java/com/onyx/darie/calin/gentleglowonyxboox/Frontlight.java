package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Context;

import com.onyx.android.sdk.api.device.FrontLightController;

public class Frontlight {
    private Frontlight() {}

    public static void turnOn(Context context) {
        // todo load last saved preset
        if (FrontLightController.getWarmLightConfigValue(context) != 0) {
            FrontLightController.openWarmLight();
        }
        if (FrontLightController.getColdLightConfigValue(context) != 0) {
            FrontLightController.openColdLight();
        }
        if (FrontLightController.getWarmLightConfigValue(context)  +
                FrontLightController.getColdLightConfigValue(context)== 0) {
            FrontLightController.openWarmLight();
            FrontLightController.openColdLight();
        }
    }

    public static boolean isOn(Context context) {
        return FrontLightController.isColdLightOn(context) || FrontLightController.isWarmLightOn((context));
    }

    public static void turnOff(QuickSettingsTile quickSettingsTile) {
        FrontLightController.closeWarmLight();
        FrontLightController.closeColdLight();
    }

    public static WarmColdSetting getWarmCold(Context context) {
        return new WarmColdSetting(
                FrontLightController.isWarmLightOn(context)?  FrontLightController.getWarmLightConfigValue(context): 0,
                FrontLightController.isColdLightOn(context)?  FrontLightController.getColdLightConfigValue(context): 0
        );
    }

    public static void setWarmCold(WarmColdSetting setting, Context context) {
        FrontLightController.setWarmLightDeviceValue(context, setting.warm);
        FrontLightController.setColdLightDeviceValue(context, setting.cold);
    }

    public static boolean hasDualFrontlight(Context context) {
        return FrontLightController.hasCTMBrightness(context);
    }

    public static WarmColdToWarmthBrightnessAdapter getWarmColdToWarmthBrightnessAdapter(Context context) {
        return new WarmColdToWarmthBrightnessAdapter(
                FrontLightController.getWarmLightValues(context),
                FrontLightController.getColdLightValues(context)
        );
    }
}
