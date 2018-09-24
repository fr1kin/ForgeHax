package com.matt.forgehax.util.command.v2.converter;

import java.util.Comparator;

public class ConverterFactory {
    public static <T> IConverter.Builder<T> newBuilder() {
        return new IConverter.Builder<>();
    }
    
    public static <T> Comparator<T> newEmptyComparator() {
        return ((o1, o2) -> 0);
    }
}
