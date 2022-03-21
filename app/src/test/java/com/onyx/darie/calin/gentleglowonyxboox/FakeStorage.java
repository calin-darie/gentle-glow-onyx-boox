package com.onyx.darie.calin.gentleglowonyxboox;

public class FakeStorage<TData> implements Storage<TData> {
    private TData data;
    public Result save(TData data) {
        this.data = data;
        return Result.success();
    }

    public Result<TData> loadOrDefault(TData defaultValue) {
        return Result.success(this.data != null? data : defaultValue);
    }
}
