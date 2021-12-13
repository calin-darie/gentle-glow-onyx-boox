package com.onyx.darie.calin.gentleglowonyxboox;

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
}

