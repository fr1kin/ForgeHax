package dev.fiki.forgehax.api.reflection.types;

import dev.fiki.forgehax.api.reflection.ReflectionHelper;
import dev.fiki.forgehax.asm.utils.asmtype.ASMField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.Objects;

import static dev.fiki.forgehax.main.Common.getLogger;

/**
 * Created on 5/25/2017 by fr1kin
 */
@Getter
@RequiredArgsConstructor
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
    if(!failed && cached == null) {
      cached = field.stream()
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
            getLogger().error("Failed to lookup field {}::{}", parentClass.getName(), getName());
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
        getLogger().error("Failed to ::get on field {}::{}", parentClass.getName(), getName());
        getLogger().error(e, e);
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
        getLogger().error("Failed to ::get on field {}::{}", parentClass.getName(), getName());
        getLogger().error(e, e);
      }
    }
    return false;
  }
  
  public boolean setStatic(V to) {
    return set(null, to);
  }
}
