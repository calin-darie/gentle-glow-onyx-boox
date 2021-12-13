package com.onyx.darie.calin.gentleglowonyxboox;

public interface LedGroup {
    Result turnOn();
    Result turnOff();
    Result<Integer> getVoltage();
    Result setOutput(int value);
}
