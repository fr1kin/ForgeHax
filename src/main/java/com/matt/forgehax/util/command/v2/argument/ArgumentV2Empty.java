package com.matt.forgehax.util.command.v2.argument;

import com.matt.forgehax.util.typeconverter.TypeConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 2/3/2018 by fr1kin
 */
public class ArgumentV2Empty<E> extends ArgumentV2<E> {
    private static final ArgumentV2Empty INSTANCE = new ArgumentV2Empty();

    public static ArgumentV2 getInstance() {
        return INSTANCE;
    }

    private final TypeConverter<E> converter = new TypeConverter<E>() {
        @Override
        public String label() {
            return "";
        }

        @Override
        public Class<E> type() {
            throw new UnsupportedOperationException("empty type");
        }

        @Override
        public E parse(String value) {
            return null;
        }

        @Override
        public String toString(Object value) {
            return "";
        }
    };

    @Override
    public String getDescription() {
        return "";
    }

    @Nonnull
    @Override
    public TypeConverter<E> getConverter() {
        return converter;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Nullable
    @Override
    public E getDefaultValue() {
        return null;
    }
}
