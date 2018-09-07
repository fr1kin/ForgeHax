package com.matt.forgehax.util.command.v2.converter;

import com.matt.forgehax.util.command.v2.converter.exceptions.StringConversionException;

import javax.annotation.Nullable;

public interface StringConverter<E> {
    /**
     * Converts a value to a string
     * @param value to convert to a string
     * @return the value as a string
     * @throws StringConversionException if there is any issue during the conversion
     */
    String toString(E value) throws StringConversionException;

    /**
     * Converts a value to a string. If it fails it uses the default string value provided
     * @param value to convert to a string
     * @param defaultTo string to default to
     * @return defaultTo if StringConversionException is thrown
     */
    default String toString(E value, @Nullable String defaultTo) {
        try {
            return toString(value);
        } catch (StringConversionException e) {
            return defaultTo;
        }
    }
}
