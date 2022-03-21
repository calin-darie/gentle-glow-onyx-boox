package com.onyx.darie.calin.gentleglowonyxboox;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    public void captureLedOutputAndComplete() {
        captureChangedLedOutput();
        reset(nativeLight);
        warmAndColdLedOutput$.onNext(ledOutput);
    }

    //todo review this
    public WarmAndColdLedOutput completeAndCaptureNewLedOutput(WarmAndColdLedOutput ledOutput) {
        captureLedOutputAndComplete();

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

    @Mock
    private NativeWarmColdLightController nativeLight;
    private Storage<WarmAndColdLedOutput> externallySetLedOutputStorage = new FakeStorage<WarmAndColdLedOutput>();

    private PublishSubject<WarmAndColdLedOutput> warmAndColdLedOutput$ =
            PublishSubject.create();

    Range<Integer> ledOutputRange = new Range<>(5, 255);
    private BrightnessAndWarmthState brightnessAndWarmthState;
    private WarmAndColdLedOutput ledOutput;
    public final Light light;

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
