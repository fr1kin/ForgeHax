package com.matt.forgehax.util.typeconverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Objects;

/**
 * Created on 3/23/2017 by fr1kin
 */
public abstract class TypeConverter<E> {
    public abstract String label();
    public abstract Class<E> type();

    public abstract E parse(String value);
    public abstract String toString(E value);

    @Nullable
    public E parseSafe(String value) {
        try {
            return parse(value);
        } catch (Throwable t) {
            return null;
        }
    }

    @Nonnull
    public String toStringSafe(E value) {
        try {
            return toString(value);
        } catch (Throwable t) {
            return String.valueOf((Object) null);
        }
    }

    public boolean isType(Class<?> clazz) {
        return Objects.equals(type(), clazz);
    }

    public boolean isAssignableFrom(Class<?> clazz) {
        return isType(clazz) || (type() != null && clazz != null && type().isAssignableFrom(clazz));
    }

    @Nullable
    public Comparator<E> comparator() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TypeConverter && Objects.equals(label(), ((TypeConverter) obj).label());
    }
}
