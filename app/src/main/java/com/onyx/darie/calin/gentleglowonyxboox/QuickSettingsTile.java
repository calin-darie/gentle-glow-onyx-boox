package com.onyx.darie.calin.gentleglowonyxboox;

import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class QuickSettingsTile extends TileService {
    private Disposable externalChangeSubscription;

    @Override
    public void onStartListening() {
        updateTile(Frontlight.isOn());
        externalChangeSubscription = Frontlight.getLightSwitchState$()
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
        externalChangeSubscription.dispose();
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
        if (!Frontlight.isOn()) {
            Frontlight.turnOn();
            updateTile(true);
        }
        else {
            Frontlight.turnOff();
            updateTile(false);
        }
     }
}
