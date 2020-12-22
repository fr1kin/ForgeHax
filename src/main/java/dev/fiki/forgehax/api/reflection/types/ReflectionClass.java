package dev.fiki.forgehax.api.reflection.types;

import dev.fiki.forgehax.asm.utils.asmtype.ASMClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
@Log4j2
public class ReflectionClass<E> {
  private final ASMClass classInfo;

  private Class<E> cached = null;
  private boolean failed = false;

  private Class<E> getCached() {
    if (!failed && cached == null) {
      cached = classInfo.getDelegates()
          .map(clazz -> {
            try {
              return (Class<E>) Class.forName(clazz.getClassName().replace('/', '.'));
            } catch (ClassNotFoundException | ClassCastException e) {
              // hopefully there is a working format
            }
            return (Class<E>) null;
          })
          .filter(Objects::nonNull)
          .findAny()
          .orElseGet(() -> {
            failed = true;
            log.error("Failed to load class \"{}\"", classInfo.getClassName());
            return null;
          });
    }
    return cached;
  }

  public String getName() {
    return classInfo.getClassName();
  }

  public Class<E> get() {
    return getCached();
  }
}
