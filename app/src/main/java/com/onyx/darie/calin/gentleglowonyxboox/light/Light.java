package com.onyx.darie.calin.gentleglowonyxboox.light;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

public interface Light {
    PublishSubject<BrightnessAndWarmth> getSetBrightnessAndWarmthRequest$();
    PublishSubject<Object> getRestoreExternallySetLedOutput$();
    PublishSubject<Integer> getApplyDeltaBrightnessRequest$();
    PublishSubject<Integer> getApplyDeltaWarmthRequest$();
    BehaviorSubject<BrightnessAndWarmth> getRestoreBrightnessAndWarmthRequest$();

    Observable<Boolean> isOn$();

    void turnOn();

    void turnOff();

    Observable<BrightnessAndWarmthState> getBrightnessAndWarmthState$();
    boolean isDeviceSupported();

    void toggleOnOff();
}
