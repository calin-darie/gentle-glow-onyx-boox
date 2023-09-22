package com.onyx.darie.calin.gentleglowonyxboox.onyx.warmandcold;

import java.util.Objects;

public class WarmAndColdLedOutput {
    public final int warm;
    public final int cold;

    public WarmAndColdLedOutput(int warm, int cold) {
        this.warm = warm;
        this.cold = cold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarmAndColdLedOutput that = (WarmAndColdLedOutput) o;
        return warm == that.warm &&
                cold == that.cold;
    }

    @Override
    public int hashCode() {
        return Objects.hash(warm, cold);
    }

    @Override
    public String toString() {
        return "{" +
                "warm=" + warm +
                ", cold=" + cold +
                '}';
    }
}
