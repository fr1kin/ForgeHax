package dev.fiki.forgehax.api.reflection.types;

import dev.fiki.forgehax.asm.utils.asmtype.ASMClass;
import dev.fiki.forgehax.main.Common;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Objects;

@RequiredArgsConstructor
@Log4j2
public final class ReflectionClass<E> {
  private final ASMClass classInfo;

  @Getter(lazy = true)
  private final Class<E> entity = lookupClass();

  @SuppressWarnings("unchecked")
  private Class<E> lookupClass() {
    return (Class<E>) classInfo.getDelegates()
        .map(this::findClass)
        .filter(Objects::nonNull)
        .findAny()
        .orElseThrow(() -> new Error("Class \"" + classInfo + "\" could not be found"));
  }

  private Class<?> findClass(ASMClass clazz) {
    try {
      return Class.forName(clazz.getClassName().replace('/', '.'));
    } catch (ClassNotFoundException | ClassCastException e) {
      Common.getLogger().debug("Class {} is not valid", clazz);
      Common.getLogger().debug(e, e);
    }
    return null;
  }

  public String getName() {
    return classInfo.getClassName();
  }

  public Class<E> get() {
    return getEntity();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ReflectionClass<?> that = (ReflectionClass<?>) o;

    return classInfo.equals(that.classInfo);
  }

  @Override
  public int hashCode() {
    return classInfo.hashCode();
  }

  @Override
  public String toString() {
    return "RC:" + classInfo;
  }
}
