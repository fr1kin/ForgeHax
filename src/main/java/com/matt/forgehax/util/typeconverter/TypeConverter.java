package com.matt.forgehax.util.typeconverter;

import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Created on 3/23/2017 by fr1kin */
public abstract class TypeConverter<E> {
  public abstract String label();

  public abstract Class<E> type();

  public abstract E parse(String value);

  public E parse(String value, @Nullable E defaultTo) {
    try {
      return parse(value);
    } catch (Throwable t) {
      return defaultTo;
    }
  }

  public abstract String toString(E value);

  public String toString(E value, @Nonnull String defaultTo) {
    try {
      return toString(value);
    } catch (Throwable t) {
      return defaultTo;
    }
  }

  @Nullable
  public E parseSafe(String value) {
    return parse(value, null);
  }

  @Nonnull
  public String toStringSafe(E value) {
    return toString(value, String.valueOf((Object) null));
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
