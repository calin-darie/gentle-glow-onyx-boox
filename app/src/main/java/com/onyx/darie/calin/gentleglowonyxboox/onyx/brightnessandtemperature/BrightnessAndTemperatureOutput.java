package com.onyx.darie.calin.gentleglowonyxboox.onyx.brightnessandtemperature;

import java.util.Objects;

public class BrightnessAndTemperatureOutput {
    public final int brightness;
    public final int temperature;

    public BrightnessAndTemperatureOutput(int brightness, int temperature) {
        this.brightness = brightness;
        this.temperature = temperature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrightnessAndTemperatureOutput that = (BrightnessAndTemperatureOutput) o;
        return brightness == that.brightness &&
               temperature == that.temperature;
    }

    @Override
    public int hashCode() {
        return Objects.hash(brightness, temperature);
    }

    @Override
    public String toString() {
        return "{" +
                "brightness=" + brightness +
                ", temperature=" + temperature +
                '}';
    }
}
