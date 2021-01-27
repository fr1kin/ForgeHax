package dev.fiki.forgehax.api.reflection.types;

import dev.fiki.forgehax.asm.utils.asmtype.ASMField;
import dev.fiki.forgehax.main.Common;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Created on 5/25/2017 by fr1kin
 */
@RequiredArgsConstructor
public final class ReflectionField<V> {
  private final ReflectionClass<?> parentClass;
  private final ASMField field;

  @Getter(lazy = true, value = AccessLevel.PACKAGE)
  private final MethodHandle getter = findGetter();
  @Getter(lazy = true, value = AccessLevel.PACKAGE)
  private final MethodHandle setter = findSetter();

  public String getName() {
    return field.getName();
  }

  private Field findField() {
    return field.getDelegates()
        .map(this::lookupField)
        .filter(Objects::nonNull)
        .findAny()
        .orElseThrow(() -> new Error("Field \"" + field + "\" could not be found"));
  }

  private Field lookupField(ASMField field) {
    try {
      Field f = parentClass.get().getDeclaredField(field.getName());
      f.setAccessible(true);
      return f;
    } catch (NoSuchFieldException | ClassCastException e) {
      Common.getLogger().debug("Field {} is not valid", field);
      Common.getLogger().debug(e, e);
    }
    return null;
  }

  @SneakyThrows
  private MethodHandle findGetter() {
    return MethodHandles.lookup().unreflectGetter(findField());
  }

  @SneakyThrows
  private MethodHandle findSetter() {
    return MethodHandles.lookup().unreflectSetter(findField());
  }

  @SneakyThrows
  public <E> V get(E instance) {
    return (V) getGetter().invoke(instance);
  }

  @SneakyThrows
  public V getStatic() {
    return (V) getGetter().invoke();
  }

  @SneakyThrows
  public <E> void set(E instance, V to) {
    getSetter().invoke(instance, to);
  }

  @SneakyThrows
  public void setStatic(V to) {
    getSetter().invoke(to);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ReflectionField<?> that = (ReflectionField<?>) o;

    return field.equals(that.field);
  }

  @Override
  public int hashCode() {
    return field.hashCode();
  }

  @Override
  public String toString() {
    return "RF:" + field;
  }
}
