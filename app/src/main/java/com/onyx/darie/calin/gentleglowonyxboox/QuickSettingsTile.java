package com.onyx.darie.calin.gentleglowonyxboox;

import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.onyx.darie.calin.gentleglowonyxboox.light.Light;
import com.onyx.darie.calin.gentleglowonyxboox.setup.GentleGlowApplication;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class QuickSettingsTile extends TileService {
    private Disposable isOnSubscription;
    private Light light;

    @Override
    public void onStartListening() {
        light = ((GentleGlowApplication)getApplication()).getDependencies().getLight();
        // todo check first time
        isOnSubscription = light.isOn$()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isOn) {
                        updateTile(isOn);
                    }
                });
    }

    @Override
    public void onStopListening() {
        isOnSubscription.dispose();
    }

    private void updateTile(boolean isLightOn) {
        int iconId = isLightOn?
                R.drawable.ic_light_on :
                R.drawable.ic_light_off;
        int descriptionId = isLightOn?
                R.string.quick_settings__light_on:
                R.string.quick_settings__light_off;
        int state = isLightOn?
                Tile.STATE_ACTIVE:
                Tile.STATE_INACTIVE;
        Tile tile = getQsTile();
        tile.setIcon(Icon.createWithResource(this, iconId));
        tile.setContentDescription(getString(descriptionId));
        tile.setState(state);
        tile.updateTile();
    }

    @Override
    public void onClick() {
        light.toggleOnOff();
     }
}
