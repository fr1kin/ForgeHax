package com.matt.forgehax.util.command.v2.converter.exceptions;

public class StringConversionException extends RuntimeException {
    public static void requireNonNull(Object o, String msg) throws StringConversionException {
        if(o == null) throw new StringConversionException(new NullPointerException(msg));
    }
    public static void requireNonNull(Object o) throws StringConversionException {
        requireNonNull(o, "null value");
    }
    public static void throwException(String message) throws StringConversionException {
        throw new StringConversionException(new Exception(message));
    }

    public StringConversionException(Throwable t) {
        super(t);
    }
}
