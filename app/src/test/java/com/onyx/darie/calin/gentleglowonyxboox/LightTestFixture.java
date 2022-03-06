package com.onyx.darie.calin.gentleglowonyxboox;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LightTestFixture {
    public WarmAndColdLedOutput setBrightnessAndWarmth(BrightnessAndWarmth brightnessAndWarmth) {
        captureWarmAndColdLedOutputWithoutCompleting(brightnessAndWarmth);
        warmAndColdLedOutput$.onNext(ledOutput);
        return ledOutput;
    }

    public WarmAndColdLedOutput captureWarmAndColdLedOutputWithoutCompleting(BrightnessAndWarmth brightnessAndWarmth) {
        reset(nativeLight);
        ArgumentCaptor<WarmAndColdLedOutput> ledOutputCaptor = ArgumentCaptor.forClass(WarmAndColdLedOutput.class);

        light.setBrightnessAndWarmthRequest$.onNext(brightnessAndWarmth);

        verify(nativeLight, atLeast(0)).setLedOutput(ledOutputCaptor.capture());

        if (ledOutputCaptor.getAllValues().size() == 0) {
            return ledOutput;
        }
        ledOutput = ledOutputCaptor.getValue();
        return ledOutput;
    }

    public void setBrightnessAndWarmthAndAssertNoChange(BrightnessAndWarmth brightnessAndWarmth) {
        reset(nativeLight);
        light.setBrightnessAndWarmthRequest$.onNext(brightnessAndWarmth);
        verify(nativeLight, never()).setLedOutput(any());
    }

    //todo review this
    public WarmAndColdLedOutput completeAndCaptureNewLedOutput(WarmAndColdLedOutput ledOutput) {
        reset(nativeLight);

        warmAndColdLedOutput$.onNext(ledOutput);

        return captureChangedLedOutput();
    }

    public WarmAndColdLedOutput captureChangedLedOutput() {
        ArgumentCaptor<WarmAndColdLedOutput> ledOutputCaptor = ArgumentCaptor.forClass(WarmAndColdLedOutput.class);
        verify(nativeLight).setLedOutput(ledOutputCaptor.capture());
        ledOutput = ledOutputCaptor.getValue();
        return ledOutput;
    }

    public void simulateOnyxSliderChange(WarmAndColdLedOutput ledOutput) {
        warmAndColdLedOutput$.onNext(ledOutput);
    }

    public BrightnessAndWarmthState getBrightnessAndWarmthState() {
        return brightnessAndWarmthState;
    }

    public void setSavedBrightnessAndWarmth(BrightnessAndWarmth value) {
        light.restoreBrightnessAndWarmthRequest$.onNext(value);
    }

    public void restoreExternallySetLedOutput() {
        resetLedOutputMocks();
        light.restoreExternallySetLedOutput$.onNext(0);
        captureChangedLedOutput();
        warmAndColdLedOutput$.onNext(ledOutput);
    }

    public void resetLedOutputMocks() {
        reset(nativeLight);
    }

    public void assertNoChange() {
        verify(nativeLight, never()).setLedOutput(any());
    }

    public double toLuxBrightnessScale(int ledOutput) {
        if (ledOutput == 0) return 0;
        return Math.pow(Math.E, (double)ledOutput/34)/17;
    }

    public double getTotalLux(WarmAndColdLedOutput ledOutput) {
        final double warmLuxScale = toLuxBrightnessScale(ledOutput.warm);
        final double coldLuxScale = toLuxBrightnessScale(ledOutput.cold);

        return warmLuxScale + coldLuxScale;
    }

    public double getWarmthPercentLux(WarmAndColdLedOutput ledOutput) {
        final double warmLuxScale = toLuxBrightnessScale(ledOutput.warm);
        final double coldLuxScale = toLuxBrightnessScale(ledOutput.cold);
        double warmPercentLuxScale = 100 * warmLuxScale / (warmLuxScale + coldLuxScale);
        return warmPercentLuxScale;
    }

    @Mock
    private NativeWarmColdLightController nativeLight;
    private Storage<WarmAndColdLedOutput> externallySetLedOutputStorage = new Storage<WarmAndColdLedOutput>(null, null) {
        private WarmAndColdLedOutput data;
        @Override
        public Result save(WarmAndColdLedOutput data) {
            this.data = data;
            return Result.success();
        }

        @Override
        public Result<WarmAndColdLedOutput> loadOrDefault(WarmAndColdLedOutput defaultValue) {
            return Result.success(this.data != null? data : defaultValue);
        }
    };

    private PublishSubject<WarmAndColdLedOutput> warmAndColdLedOutput$ =
            PublishSubject.create();
    private LightCommandSource commandSource = new LightCommandSource() {
        @Override
        public Flowable<BrightnessAndWarmth> getBrightnessAndWarmthChangeRequest$() {
            return null;
        }

        @Override
        public Observable<Integer> getApplyDeltaBrightnessRequest$() {
            return null;
        }

        @Override
        public Observable<Integer> getApplyDeltaWarmthRequest$() {
            return null;
        }

        @Override
        public Observable getRestoreExternalSettingRequest$() {
            return null;
        }

        @Override
        public Observable<BrightnessAndWarmth> getBrightnessAndWarmthRestoreFromStorageRequest$() {
            return light.restoreBrightnessAndWarmthRequest$;
        }
    };

    Range<Integer> ledOutputRange = new Range<>(5, 255);
    private BrightnessAndWarmthState brightnessAndWarmthState;
    private WarmAndColdLedOutput ledOutput;
    private final Light light;

    public LightTestFixture() {
        MockitoAnnotations.openMocks(this);

        when(nativeLight.getWarmAndColdLedOutput$())
                .thenReturn(warmAndColdLedOutput$);
        OnyxBrightnessAndWarmthToWarmAndColdLedOutputAdapter adapter =
                new OnyxBrightnessAndWarmthToWarmAndColdLedOutputAdapter(ledOutputRange);
        light = new Light(nativeLight, adapter, externallySetLedOutputStorage);
        light.getBrightnessAndWarmthState$().subscribe(brightnessAndWarmthState -> this.brightnessAndWarmthState = brightnessAndWarmthState);
    }
}
