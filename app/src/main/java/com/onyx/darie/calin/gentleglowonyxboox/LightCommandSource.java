package com.onyx.darie.calin.gentleglowonyxboox;

import io.reactivex.rxjava3.core.Flowable;

public interface LightCommandSource {
    Flowable<BrightnessAndWarmth> getBrightnessAndWarmthChangeRequest$();
}
