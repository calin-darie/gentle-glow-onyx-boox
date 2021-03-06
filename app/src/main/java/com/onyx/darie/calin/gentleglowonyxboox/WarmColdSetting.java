package com.onyx.darie.calin.gentleglowonyxboox;

import java.util.Objects;

@Deprecated
/** to be replaced by {@link WarmAndColdLedOutput} **/
public class WarmColdSetting {
    public final int warm;
    public final int cold;

    public WarmColdSetting(int warm, int cold) {
        this.warm = warm;
        this.cold = cold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarmColdSetting that = (WarmColdSetting) o;
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

