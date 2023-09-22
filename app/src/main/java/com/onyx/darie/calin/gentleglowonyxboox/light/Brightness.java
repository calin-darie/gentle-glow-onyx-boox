package com.onyx.darie.calin.gentleglowonyxboox.light;

public class Brightness extends PercentValue {
    public Brightness(int percent) {
        super(percent < 1? 1 : percent);
    };
}
