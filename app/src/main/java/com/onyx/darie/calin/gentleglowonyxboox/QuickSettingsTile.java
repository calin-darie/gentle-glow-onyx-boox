package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.onyx.android.sdk.api.device.FrontLightController;

public class QuickSettingsTile extends TileService {
    @Override
    public void onStartListening() {
        updateTile(Frontlight.isOn(this));
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
        if (!Frontlight.isOn(this)) {
            Frontlight.turnOn(this);
            updateTile(true);
        }
        else {
            Frontlight.turnOff();
            updateTile(false);
        }
     }
}
