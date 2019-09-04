package com.matt.forgehax.util.typeconverter;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Created on 6/3/2017 by fr1kin
 */
public class TypeConverterRegistry {

  private static final Map<Class<?>, TypeConverter<?>> REGISTRY = Maps.newHashMap();

  public static <T> void registerAll(final TypeConverter<T> converter, Class<?>... types) {
    Collection<Class<?>> all = Sets.newHashSet(types);
    all.add(converter.type());
    all.forEach(t -> REGISTRY.put(t, converter));
  }

  public static <T> void register(TypeConverter<T> converter) {
    registerAll(converter, converter.type());
  }

  public static <T> void unregister(final TypeConverter<T> converter) {
    REGISTRY.forEach(
      (k, v) -> {
        if (v.equals(converter)) {
          REGISTRY.remove(k);
        }
      });
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public static <T> TypeConverter<T> get(Class<?> type) {
    try {
      for (TypeConverter<?> converter : REGISTRY.values()) {
        if (converter.isType(type)) {
          return (TypeConverter<T>) converter;
        }
      }
    } catch (Throwable t) {
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public static <T> TypeConverter<T> getByName(String className) {
    return REGISTRY
      .entrySet()
      .stream()
      .filter(entry -> Objects.equals(className, entry.getKey().getName()))
      .findFirst()
      .map(
        entry -> {
          try {
            return (TypeConverter<T>) entry.getValue();
          } catch (Throwable t) {
            return null;
          }
        })
      .orElse(null);
  }

  static {
    // Will add both the Java object and primitive type
    registerAll(TypeConverters.BOOLEAN, boolean.class);
    registerAll(TypeConverters.BYTE, byte.class);
    registerAll(TypeConverters.CHARACTER, char.class);
    registerAll(TypeConverters.DOUBLE, double.class);
    registerAll(TypeConverters.FLOAT, float.class);
    registerAll(TypeConverters.INTEGER, int.class);
    registerAll(TypeConverters.LONG, long.class);
    registerAll(TypeConverters.SHORT, short.class);
    register(TypeConverters.STRING);
  }
}
