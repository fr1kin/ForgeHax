package com.matt.forgehax.util.command.v2.converter;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Converters {
  private static final List<IConverter<?>> CONVERTERS = Lists.newArrayList();

  public static void register(IConverter<?> converter) {
    Objects.requireNonNull(converter);
    if (!exists(converter)) CONVERTERS.add(converter);
  }

  public static void unregister(IConverter<?> converter) {
    CONVERTERS.remove(converter);
  }

  public static void unregister(Class<?> clazz) {
    get(clazz).ifPresent(Converters::unregister);
  }

  @SuppressWarnings("unchecked")
  public static <T> Optional<IConverter<T>> get(final Class<T> clazz) {
    Objects.requireNonNull(clazz);
    return CONVERTERS
        .stream()
        .filter(c -> clazz.equals(c.getType()) || clazz.equals(c.getPrimitiveType()))
        .findFirst()
        .map(c -> (IConverter<T>) c);
  }

  @SuppressWarnings("unchecked")
  public static <T> Optional<IConverter<T>> get(T o) {
    return o == null ? Optional.empty() : get((Class<T>) o.getClass());
  }

  public static boolean exists(Class<?> clazz) {
    return get(clazz).isPresent();
  }

  public static boolean exists(IConverter<?> converter) {
    return get(converter.getType()).isPresent();
  }

  static {
    register(DefaultConverters.BOOLEAN);
    register(DefaultConverters.BYTE);
    register(DefaultConverters.CHARACTER);
    register(DefaultConverters.DOUBLE);
    register(DefaultConverters.FLOAT);
    register(DefaultConverters.INTEGER);
    register(DefaultConverters.LONG);
    register(DefaultConverters.SHORT);
    register(DefaultConverters.STRING);
  }
}
