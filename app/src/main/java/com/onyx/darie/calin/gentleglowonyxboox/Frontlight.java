package com.onyx.darie.calin.gentleglowonyxboox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import com.onyx.android.sdk.api.device.FrontLightController;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

public class Frontlight {
    private Frontlight() {}

    @SuppressLint("StaticFieldLeak") // because we're storing the application context
    private static Context applicationContext;

    public static void turnOn() {
        WarmColdSetting warmColdSetting = latestSetWarmColdSetting != null ?
                latestSetWarmColdSetting :
                getWarmCold();
        if (warmColdSetting.warm == 0 && warmColdSetting.cold == 0) {
            FrontLightController.openWarmLight();
            FrontLightController.openColdLight();
        }
        if (warmColdSetting.warm != 0) {
            FrontLightController.openWarmLight();
        }
        if (warmColdSetting.cold != 0) {
            FrontLightController.openColdLight();
        }
    }

    public static boolean isOn() {
        return FrontLightController.isColdLightOn(applicationContext) ||
                FrontLightController.isWarmLightOn((applicationContext));
    }

    public static void turnOff() {
        FrontLightController.closeWarmLight();
        FrontLightController.closeColdLight();
    }

    public static WarmColdSetting getWarmCold() {
        return new WarmColdSetting(
                FrontLightController.isWarmLightOn(applicationContext)?  FrontLightController.getWarmLightConfigValue(applicationContext): 0,
                FrontLightController.isColdLightOn(applicationContext)?  FrontLightController.getColdLightConfigValue(applicationContext): 0
        );
    }

    private static WarmColdSetting nextWarmCold;
    private static WarmColdSetting latestSetWarmColdSetting;
    private static boolean isChangePending;
    public static void setWarmCold(final WarmColdSetting setting) {
        if (isChangePending) {
            nextWarmCold = setting;
            return;
        }
        latestSetWarmColdSetting = setting;
        nextWarmCold = null;
        final WarmColdSetting current = getWarmCold();
        final boolean isWarmChange = current.warm != setting.warm;
        final boolean isColdChange = current.cold != setting.cold;
        isChangePending = isWarmChange || isColdChange;
        if (!isChangePending)
            return;

        warmColdSetting$
                .take(1)
                .subscribe(new Consumer<WarmColdSetting>() {
            @Override
            public void accept(WarmColdSetting _) {
                isChangePending = false;
                if (nextWarmCold != null) {
                    setWarmCold(nextWarmCold);
                }
            }
        });
        if (isWarmChange) {
            FrontLightController.setWarmLightDeviceValue(applicationContext, setting.warm);
        }
        if (isColdChange) {
            FrontLightController.setColdLightDeviceValue(applicationContext, setting.cold);
        }
    }

    public static boolean hasDualFrontlight() {
        return FrontLightController.hasCTMBrightness(applicationContext);
    }

    public static WarmColdToWarmthBrightnessAdapter getWarmColdToWarmthBrightnessAdapter() {
        return new WarmColdToWarmthBrightnessAdapter(
                FrontLightController.getWarmLightValues(applicationContext),
                FrontLightController.getColdLightValues(applicationContext)
        );
    }

    public static void ensureTurnedOn() {
        if (!isOn()) {
            turnOn();
        }
    }

    private static Observable<Boolean> lightSwitchState$;
    public static Observable<Boolean> getLightSwitchState$() {
        return lightSwitchState$;
    }

    private static Observable<WarmColdSetting> warmColdSetting$;
    public static Observable<WarmColdSetting> getWarmColdExternalChange$() {
        return warmColdSetting$
                .filter(new Predicate<WarmColdSetting>() {
                    @Override
                    public boolean test(@NonNull WarmColdSetting warmColdSetting) {
                        return !isChangePending && !warmColdSetting.equals(latestSetWarmColdSetting);
                    }
                });
    }

    public static void initContext(Context context) {
        Frontlight.applicationContext = context;
        initFrontlightStateObservable(context);
    }


    private static void initFrontlightStateObservable(Context context) {
        warmColdSetting$ =
                ContentObserverSubscriber
                        .create(
                            context.getContentResolver(),
                            new Uri[]{
                                    Uri.parse("content://settings/system/screen_cold_brightness"),
                                    Uri.parse("content://settings/system/screen_warm_brightness"),
                            },
                            new Function<Uri, WarmColdSetting>() {
                                @Override
                                public WarmColdSetting apply(@NonNull Uri uri) {
                                    return Frontlight.getWarmCold();
                                }
                            }
                        )
                        .share();
        lightSwitchState$ =
                ContentObserverSubscriber
                        .create(
                            context.getContentResolver(),
                            new Uri[]{
                                    Uri.parse("content://settings/system/cold_brightness_state_key"),
                                    Uri.parse("content://settings/system/warm_brightness_state_key"),
                            },
                            new Function<Uri, Boolean>() {
                                @Override
                                public Boolean apply(@NonNull Uri uri) {
                                    return Frontlight.isOn();
                                }
                            }
                        )
                        .share();
    }
}
