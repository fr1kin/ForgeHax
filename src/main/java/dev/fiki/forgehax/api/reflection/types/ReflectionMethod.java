package dev.fiki.forgehax.api.reflection.types;

import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Created on 5/25/2017 by fr1kin
 */

@Getter
@RequiredArgsConstructor
@Log4j2
public class ReflectionMethod<V> {
  private final ReflectionClass<?> parentClass;
  private final ASMMethod method;

  private Method cached = null;
  private boolean failed = false;

  public String getName() {
    return method.getName();
  }

  private Method getCached() {
    if (!failed && cached == null) {
      cached = method.getDelegates()
          .map(type -> {
            for (Method classMethod : parentClass.get().getDeclaredMethods()) {
              Type methodDescriptor = Type.getType(classMethod);
              if (type.getName().equals(classMethod.getName()) && type.getDescriptor().equals(methodDescriptor)) {
                classMethod.setAccessible(true);
                return classMethod;
              }
            }
            return null;
          })
          .filter(Objects::nonNull)
          .findAny()
          .orElseGet(() -> {
            failed = true;
            log.error("Failed to lookup method {}::{}", parentClass.getName(), getName());
            return null;
          });
    }
    return cached;
  }

  public <E> V invoke(E instance, Object... args) {
    try {
      //noinspection unchecked
      return (V) Objects.requireNonNull(getCached()).invoke(instance, args);
    } catch (Throwable t) {
      if (!failed) {
        failed = true;
        log.error("Invoke failed for method {}::{}", parentClass.getName(), getName());
        log.error(t, t);
      }
    }
    return null;
  }

  public V invokeStatic(Object... args) {
    return invoke(null, args);
  }
}
