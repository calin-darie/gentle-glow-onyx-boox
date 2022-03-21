package com.onyx.darie.calin.gentleglowonyxboox;

public interface Storage<T> {
    Result save(T data);

    Result<T> loadOrDefault(T defaultValue);
}
