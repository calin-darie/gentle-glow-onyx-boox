package com.onyx.darie.calin.gentleglowonyxboox;

public final class Range<T extends Comparable<? super T>> {
    private final T lower;
    private final T upper;

    public Range(T lower, T upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public T getLower() { return lower;}

    public T getUpper() { return upper; }

    @Override
    public String toString() {
        return "[" + lower + ",  " + upper +']';
    }
}
