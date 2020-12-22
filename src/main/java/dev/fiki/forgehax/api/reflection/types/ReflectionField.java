package dev.fiki.forgehax.api.reflection.types;

import dev.fiki.forgehax.api.reflection.ReflectionHelper;
import dev.fiki.forgehax.asm.utils.asmtype.ASMField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Created on 5/25/2017 by fr1kin
 */
@Getter
@RequiredArgsConstructor
@Log4j2
public class ReflectionField<V> {
  private final ReflectionClass<?> parentClass;
  private final ASMField field;

  @Setter
  private boolean definalize;
  private Field cached;
  private boolean failed;

  public String getName() {
    return field.getName();
  }

  private Field getCached() {
    if (!failed && cached == null) {
      cached = field.getDelegates()
          .map(type -> {
            Field ret = null;
            try {
              ret = parentClass.get().getDeclaredField(type.getName());
              ret.setAccessible(true);
              if (definalize) {
                ReflectionHelper.makeMutable(ret);
              }
            } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
              ;
            }
            return ret;
          })
          .filter(Objects::nonNull)
          .findAny()
          .orElseGet(() -> {
            failed = true;
            log.error("Failed to lookup field {}::{}", parentClass.getName(), getName());
            return null;
          });
    }
    return cached;
  }

  public <E> V get(E instance, V defaultValue) {
    try {
      //noinspection unchecked
      return (V) getCached().get(instance);
    } catch (Exception e) {
      if (!failed) {
        failed = true;
        log.error("Failed to ::get on field {}::{}", parentClass.getName(), getName());
        log.error(e, e);
      }
    }
    return defaultValue;
  }

  public <E> V get(E instance) {
    return get(instance, null);
  }

  public V getStatic(V defaultValue) {
    return get(null, defaultValue);
  }

  public V getStatic() {
    return get(null);
  }

  public <E> boolean set(E instance, V to) {
    try {
      getCached().set(instance, to);
      return true;
    } catch (Exception e) {
      if (!failed) {
        failed = true;
        log.error("Failed to ::get on field {}::{}", parentClass.getName(), getName());
        log.error(e, e);
      }
    }
    return false;
  }

  public boolean setStatic(V to) {
    return set(null, to);
  }
}
