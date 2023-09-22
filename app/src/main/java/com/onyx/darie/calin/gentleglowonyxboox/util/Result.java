package com.onyx.darie.calin.gentleglowonyxboox.util;

import java.util.Objects;

public class Result<T> {
    public final String error;
    public final T value;

    public boolean hasError() { return error != null; }

    public static Result error(String error) { return new Result(error); }
    public static <T> Result success(T value) { return new Result(value); }
    public static Result success() { return new Result(null); }

    private Result(String error) {
        this.error = error;
        this.value = null;
    }

    private Result(T value) {
        this.error = null;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Result)) return false;
        Result<?> result = (Result<?>) o;
        return Objects.equals(error, result.error) && Objects.equals(value, result.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error, value);
    }
}

