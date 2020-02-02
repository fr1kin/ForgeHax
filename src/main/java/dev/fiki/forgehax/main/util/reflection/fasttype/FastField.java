package dev.fiki.forgehax.main.util.reflection.fasttype;

import java.lang.reflect.Field;

import dev.fiki.forgehax.main.util.reflection.ReflectionHelper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static dev.fiki.forgehax.main.Common.getLogger;

/**
 * Created on 5/25/2017 by fr1kin
 */
@Getter
public class FastField<V> {

  private Class<?> parent;

  private String mcp;
  private String srg;

  private boolean definalize;
  private Field cached;
  private boolean failed;

  @Builder
  private FastField(Class<?> parent, String mcp, String srg, boolean definalize) {
    this.parent = parent;
    this.mcp = mcp;
    this.srg = srg;
    this.definalize = definalize;
  }

  private Field getCached() {
    if(!failed && cached == null) {
      try {
        cached = parent.getDeclaredField(getSrg());
        cached.setAccessible(true);
        if(definalize) {
          ReflectionHelper.makeMutable(cached);
        }
      } catch (NoSuchFieldException | IllegalAccessException e) {
        // try again but with the mcp name this time
        try {
          cached = parent.getDeclaredField(getMcp());
          cached.setAccessible(true);
          if(definalize) {
            ReflectionHelper.makeMutable(cached);
          }
        } catch (NoSuchFieldException | IllegalAccessException ex) {
          failed = true; // stop trying to lookup this field
          getLogger().error("Failed to lookup field {}::{}", parent.getTypeName(), getMcp());
          getLogger().error(ex, ex);
        }
      }
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
        getLogger().error("Failed to ::get on field {}::{}", parent.getTypeName(), getMcp());
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
        getLogger().error("Failed to ::get on field {}::{}", parent.getTypeName(), getMcp());
        getLogger().error(e, e);
      }
    }
    return false;
  }
  
  public boolean setStatic(V to) {
    return set(null, to);
  }

  public static class FastFieldBuilder<E> {
    public FastFieldBuilder<E> name(String name) {
      return srg(name).mcp(name);
    }

    private FastFieldBuilder<E> definalize(boolean b) {
      this.definalize = b;
      return this;
    }

    public FastFieldBuilder<E> definalize() {
      return definalize(true);
    }

    public <T> FastField<T> build() {
      return new FastField<T>(parent, mcp, srg, definalize);
    }
  }
}
