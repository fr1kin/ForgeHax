package dev.fiki.forgehax.main.util.reflection.fasttype;

import lombok.Builder;
import lombok.Getter;

import static dev.fiki.forgehax.main.Common.getLogger;

@Getter
public class FastClass<E> {
  private String className;

  private Class<E> cached;
  private boolean failed;

  @Builder
  private FastClass(String className) {
    this.className = className;
    this.cached = null;
    this.failed = false;
  }

  private Class<E> getCached() {
    if(!failed && cached == null) {
      try {
        //noinspection unchecked
        cached = (Class<E>) Class.forName(className);
      } catch (ClassNotFoundException e) {
        failed = true;
        getLogger().error("Failed to load class \"{}\"", className);
      }
    }
    return cached;
  }

  public Class<E> getInstance() {
    return getCached();
  }

  public static class FastClassBuilder<E> {
    public FastClassBuilder<E> internalClassName(String internalClassName) {
      return className(internalClassName.replace('/', '.'));
    }

    public <T> FastClass<T> build() {
      return new FastClass<>(className);
    }
  }
}
