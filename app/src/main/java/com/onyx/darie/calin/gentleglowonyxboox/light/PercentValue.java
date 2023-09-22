package com.onyx.darie.calin.gentleglowonyxboox.light;

import com.onyx.darie.calin.gentleglowonyxboox.util.Result;

import java.util.Objects;

class PercentValue {
    public final int value;

    public PercentValue(int value) {
        if (! isValidPercent(value))
            throw new IllegalArgumentException("value out of bounds: " + value + " should represent a percent");
        this.value = value;
    }

    private boolean isValidPercent(int value) { return value >= 0 && value <=100; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PercentValue)) return false;
        PercentValue that = (PercentValue) o;
        return value == that.value;
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value + "%"; }

    /** TPercentValue must be an instance of PercentValue **/
    public static <TPercentValue> Result<TPercentValue> withDelta(TPercentValue oldValue, int delta) {
        final int newValue = ((PercentValue)oldValue).value + delta;
        if (newValue < 0 || newValue > 100) {
            return Result.error("out of range");
        }
        try {
            TPercentValue newInstance = (TPercentValue) oldValue.getClass()
                    .getDeclaredConstructor(int.class).newInstance(newValue);
            return Result.success(newInstance);
        } catch (Exception e) {
            return Result.error("could not instantiate new value");
        }
    }
}
