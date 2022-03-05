package com.onyx.darie.calin.gentleglowonyxboox;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;

public interface LightCommandSource {
    Flowable<BrightnessAndWarmth> getBrightnessAndWarmthChangeRequest$();
    Observable<Integer> getApplyDeltaBrightnessRequest$();
    Observable<Integer> getApplyDeltaWarmthRequest$();
    Observable getRestoreExternalSettingRequest$();
    Observable<BrightnessAndWarmth> getBrightnessAndWarmthRestoreFromStorageRequest$();
}
