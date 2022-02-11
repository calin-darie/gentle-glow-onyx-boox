package com.onyx.darie.calin.gentleglowonyxboox;

public final class Range<T extends Comparable<? super T>> {
    private final T lower;
    private final T upper;

    public Range(T lower, T upper) {
        if (lower.compareTo(upper) > 0)
            throw new Error("invalid range");
        this.lower = lower;
        this.upper = upper;
    }

    public T getLower() { return lower;}

    public T getUpper() { return upper; }

    @Override
    public String toString() {
        return "[" + lower + ",  " + upper +']';
    }

    public boolean containsInclusive(T v) {
        return lower.compareTo(v) <= 0 && v.compareTo(upper) <= 0;
    }
}
