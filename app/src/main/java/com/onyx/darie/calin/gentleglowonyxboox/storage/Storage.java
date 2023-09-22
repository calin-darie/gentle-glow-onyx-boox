package com.onyx.darie.calin.gentleglowonyxboox.storage;

import com.onyx.darie.calin.gentleglowonyxboox.util.Result;

public interface Storage<T> {
    Result save(T data);

    Result<T> loadOrDefault(T defaultValue);
}
