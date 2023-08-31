package com.onyx.darie.calin.gentleglowonyxboox;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subjects.PublishSubject;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LightTestFixture {
    @Mock
    private NativeWarmColdLightController nativeLight;
    private PublishSubject<BrightnessAndWarmth> setBrightnessAndWarmthRequest$ =
            PublishSubject.create();
    private PublishSubject<WarmAndColdLedOutput> warmAndColdLedOutput$ =
            PublishSubject.create();
    private LightCommandSource commandSource = new LightCommandSource() {
        @Override
        public Flowable<BrightnessAndWarmth> getBrightnessAndWarmthChangeRequest$() {
            return setBrightnessAndWarmthRequest$.toFlowable(BackpressureStrategy.BUFFER);
        }
    };
    Range<Integer> ledOutputRange = new Range<>(5, 255);

    public LightTestFixture() {
        MockitoAnnotations.openMocks(this);

        when(nativeLight.getWarmAndColdLedOutput$())
                .thenReturn(warmAndColdLedOutput$);
        if (warmAndColdLedOutput$ == null) {
            throw new Error("wtf field null");
        }
        if (nativeLight.getWarmAndColdLedOutput$() == null) {
            throw new Error("wtf stubbed method returns null");
        }
        OnyxBrightnessAndWarmthToWarmAndColdLedOutputAdapter adapter =
                new OnyxBrightnessAndWarmthToWarmAndColdLedOutputAdapter(ledOutputRange);
        new Light(commandSource, nativeLight, adapter);
    }

    public WarmAndColdLedOutput captureWarmAndColdOutput(BrightnessAndWarmth brightnessAndWarmth) {
        WarmAndColdLedOutput ledOutput = captureWarmAndColdLedOutputWithoutCompleting(brightnessAndWarmth);
        warmAndColdLedOutput$.onNext(ledOutput);
        return ledOutput;
    }

    public WarmAndColdLedOutput captureWarmAndColdLedOutputWithoutCompleting(BrightnessAndWarmth brightnessAndWarmth) {
        reset(nativeLight);
        ArgumentCaptor<WarmAndColdLedOutput> ledOutputCaptor = ArgumentCaptor.forClass(WarmAndColdLedOutput.class);

        setBrightnessAndWarmthRequest$.onNext(brightnessAndWarmth);

        verify(nativeLight).setLedOutput(ledOutputCaptor.capture());
        WarmAndColdLedOutput ledOutput = ledOutputCaptor.getValue();
        return ledOutput;
    }

    public void setBrightnessAndWarmthAndAssertNoChange(BrightnessAndWarmth brightnessAndWarmth) {
        reset(nativeLight);
        setBrightnessAndWarmthRequest$.onNext(brightnessAndWarmth);
        verify(nativeLight, never()).setLedOutput(any());
    }

    public WarmAndColdLedOutput completeAndCaptureNewLedOutput(WarmAndColdLedOutput ledOutput) {
        reset(nativeLight);
        ArgumentCaptor<WarmAndColdLedOutput> ledOutputCaptor = ArgumentCaptor.forClass(WarmAndColdLedOutput.class);

        warmAndColdLedOutput$.onNext(ledOutput);

        verify(nativeLight).setLedOutput(ledOutputCaptor.capture());
        return ledOutputCaptor.getValue();
    }
}
