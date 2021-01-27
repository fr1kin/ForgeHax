package dev.fiki.forgehax.api.reflection.types;

import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.main.Common;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Created on 5/25/2017 by fr1kin
 */

@RequiredArgsConstructor
public final class ReflectionMethod<V> {
  private final ReflectionClass<?> parentClass;
  private final ASMMethod method;

  @Getter(lazy = true, value = AccessLevel.PACKAGE)
  private final MethodHandle invoker = findInvoker();

  public String getName() {
    return method.getName();
  }

  private Method lookupMethod() {
    return method.getDelegates()
        .map(this::findMethod)
        .filter(Objects::nonNull)
        .findAny()
        .orElseThrow(() -> new Error("Method \"" + method + "\" could not be found"));
  }

  private Method findMethod(ASMMethod method) {
    for (Method m : parentClass.get().getDeclaredMethods()) {
      Type methodDescriptor = Type.getType(m);
      if (method.getName().equals(m.getName()) && method.getDescriptor().equals(methodDescriptor)) {
        m.setAccessible(true);
        return m;
      }
    }
    Common.getLogger().debug("Method {} is not valid", method);
    return null;
  }

  @SneakyThrows
  private MethodHandle findInvoker() {
    return MethodHandles.lookup().unreflect(lookupMethod());
  }

  @SneakyThrows
  public V invoke(Object... args) {
    return (V) getInvoker().invokeWithArguments(args);
  }

  @SneakyThrows
  public V invokeStatic(Object... args) {
    return (V) getInvoker().invokeWithArguments(args);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ReflectionMethod<?> that = (ReflectionMethod<?>) o;

    return method.equals(that.method);
  }

  @Override
  public int hashCode() {
    return method.hashCode();
  }

  @Override
  public String toString() {
    return "RM:" + method;
  }
}
