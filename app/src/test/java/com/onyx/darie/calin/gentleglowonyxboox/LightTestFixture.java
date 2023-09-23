package com.onyx.darie.calin.gentleglowonyxboox;

import com.onyx.darie.calin.gentleglowonyxboox.light.Brightness;
import com.onyx.darie.calin.gentleglowonyxboox.light.BrightnessAndWarmth;
import com.onyx.darie.calin.gentleglowonyxboox.light.BrightnessAndWarmthState;
import com.onyx.darie.calin.gentleglowonyxboox.light.LightImpl;
import com.onyx.darie.calin.gentleglowonyxboox.light.NativeLightController;
import com.onyx.darie.calin.gentleglowonyxboox.light.Warmth;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold.BrightnessAndWarmthToWarmAndColdLedOutputAdapter;
import com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold.WarmAndColdLedOutput;
import com.onyx.darie.calin.gentleglowonyxboox.storage.Storage;
import com.onyx.darie.calin.gentleglowonyxboox.util.Range;
import com.onyx.darie.calin.gentleglowonyxboox.util.Result;

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
        complete(ledOutput);
        return ledOutput;
    }

    public WarmAndColdLedOutput captureWarmAndColdLedOutputWithoutCompleting(BrightnessAndWarmth brightnessAndWarmth) {
        resetLedOutputMocks();
        ArgumentCaptor<WarmAndColdLedOutput> ledOutputCaptor = ArgumentCaptor.forClass(WarmAndColdLedOutput.class);

        light.getSetBrightnessAndWarmthRequest$().onNext(brightnessAndWarmth);

        verify(nativeLight, atLeast(0)).setOutput(ledOutputCaptor.capture());

        if (ledOutputCaptor.getAllValues().size() == 0) {
            return ledOutput;
        }
        ledOutput = ledOutputCaptor.getValue();
        return ledOutput;
    }

    public void setBrightnessAndWarmthAndAssertNoChange(BrightnessAndWarmth brightnessAndWarmth) {
        resetLedOutputMocks();
        light.getSetBrightnessAndWarmthRequest$().onNext(brightnessAndWarmth);
        verify(nativeLight, never()).setOutput(any());
    }

    public void captureLedOutputAndComplete() {
        captureChangedLedOutput();
        resetLedOutputMocks();
        complete(ledOutput);
    }

    public void complete(WarmAndColdLedOutput ledOutput) {
        resetLedOutputMocks();
        warmAndColdLedOutput$.onNext(ledOutput);
        setLedOutputResult$.onNext(Result.success());
    }

    public WarmAndColdLedOutput captureChangedLedOutput() {
        ArgumentCaptor<WarmAndColdLedOutput> ledOutputCaptor = ArgumentCaptor.forClass(WarmAndColdLedOutput.class);
        verify(nativeLight).setOutput(ledOutputCaptor.capture());
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
        light.getRestoreBrightnessAndWarmthRequest$().onNext(value);
    }

    public void restoreExternallySetLedOutput() {
        resetLedOutputMocks();
        light.getRestoreExternallySetLedOutput$().onNext(0);
        captureChangedLedOutput();
        warmAndColdLedOutput$.onNext(ledOutput);
    }

    public void resetLedOutputMocks() {
        reset(nativeLight);
        when(nativeLight.setOutput(any())).thenReturn(setLedOutputResult$.firstOrError());
    }

    public void assertNoChange() {
        verify(nativeLight, never()).setOutput(any());
    }

    @Mock
    private NativeLightController<WarmAndColdLedOutput> nativeLight;
    private Storage<WarmAndColdLedOutput> externallySetLedOutputStorage = new FakeStorage<WarmAndColdLedOutput>();

    private PublishSubject<WarmAndColdLedOutput> warmAndColdLedOutput$ =
            PublishSubject.create();
    private PublishSubject<Result> setLedOutputResult$ = PublishSubject.create();

    Range<Integer> ledOutputRange = new Range<>(5, 255);
    private BrightnessAndWarmthState brightnessAndWarmthState;
    private WarmAndColdLedOutput ledOutput;
    public final LightImpl<WarmAndColdLedOutput> light;

    public LightTestFixture() {
        MockitoAnnotations.openMocks(this);

        resetLedOutputMocks();
        when(nativeLight.getOutput$())
                .thenReturn(warmAndColdLedOutput$);
        BrightnessAndWarmthToWarmAndColdLedOutputAdapter adapter =
                new BrightnessAndWarmthToWarmAndColdLedOutputAdapter(ledOutputRange);
        light = new LightImpl<WarmAndColdLedOutput>(nativeLight, adapter, externallySetLedOutputStorage);
        light.getBrightnessAndWarmthState$().subscribe(brightnessAndWarmthState -> this.brightnessAndWarmthState = brightnessAndWarmthState);
        light.getRestoreBrightnessAndWarmthRequest$().onNext(new BrightnessAndWarmth(new Brightness(51), new Warmth(51)));
    }
}
