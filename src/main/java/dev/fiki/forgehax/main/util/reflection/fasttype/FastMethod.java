package dev.fiki.forgehax.main.util.reflection.fasttype;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import lombok.*;

import static dev.fiki.forgehax.main.Common.getLogger;

/**
 * Created on 5/25/2017 by fr1kin
 */

@Getter
public class FastMethod<V> {
  private Class<?> parent;

  private String mcp;
  private String srg;

  private Method cached = null;
  private boolean failed = false;
  private Class<?>[] arguments;

  @Builder
  private FastMethod(Class<?> parent, String mcp, String srg, Class<?>[] arguments) {
    this.parent = parent;
    this.mcp = mcp;
    this.srg = srg;
    this.arguments = arguments;
  }

  public String getName() {
    return mcp == null ? srg : mcp;
  }

  private Method getCached() {
    if(!failed && cached == null) {
      try {
        cached = parent.getDeclaredMethod(getSrg(), arguments);
        cached.setAccessible(true);
      } catch (NoSuchMethodException e) {
        // try again but with the mcp name this time
        try {
          cached = parent.getDeclaredMethod(getMcp(), arguments);
          cached.setAccessible(true);
        } catch (NoSuchMethodException ex) {
          failed = true; // stop trying to lookup this field
          getLogger().error("Failed to lookup method {}::{}", parent.getTypeName(), getMcp());
          getLogger().error(ex, ex);
        }
      }
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
        getLogger().error("Invoke failed for method {}::{}", parent.getTypeName(), getMcp());
        getLogger().error(t, t);
      }
    }
    return null;
  }
  
  public V invokeStatic(Object... args) {
    return invoke(null, args);
  }

  public static class FastMethodBuilder<E> {
    private List<Class<?>> arguments = new ArrayList<>();

    public FastMethodBuilder<E> name(String name) {
      return srg(name).mcp(name);
    }

    public FastMethodBuilder<E> arguments(Class<?>... args) {
      this.arguments.addAll(Arrays.asList(args));
      return this;
    }

    public FastMethodBuilder<E> argument(Class<?> arg) {
      this.arguments.add(arg);
      return this;
    }

    public FastMethodBuilder<E> noArguments() {
      return this;
    }

    private FastMethodBuilder<E> cached(Method method) {
      return this;
    }

    private FastMethodBuilder<E> failed(boolean b) {
      return this;
    }

    public <T> FastMethod<T> build() {
      return new FastMethod<T>(parent, mcp, srg, arguments.toArray(new Class[0]));
    }
  }
}
