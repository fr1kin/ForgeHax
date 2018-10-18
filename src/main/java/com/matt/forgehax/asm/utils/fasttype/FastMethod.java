package com.matt.forgehax.asm.utils.fasttype;

import com.matt.forgehax.asm.utils.ASMStackLogger;
import com.matt.forgehax.asm.utils.environment.State;
import com.matt.forgehax.asm.utils.name.IName;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import joptsimple.internal.Strings;

/** Created on 5/25/2017 by fr1kin */
public class FastMethod<V> extends FastType<Method> {
  private final Class<?>[] parameters;

  public FastMethod(Class<?> insideClass, IName<String> name, Class<?>[] parameters) {
    super(insideClass, name);
    this.parameters = Arrays.copyOf(parameters, parameters.length);
  }

  public Class<?>[] getParameters() {
    return parameters;
  }

  public <E> V invoke(E instance, V defaultValue, Object... args) {
    try {
      if (attemptLookup()) return (V) type.invoke(instance, args);
    } catch (Exception e) {
      if (printOnce.compareAndSet(false, true)) ASMStackLogger.printStackTrace(e);
    }
    return defaultValue;
  }

  public <E> V invoke(E instance, Object... args) {
    return invoke(instance, null, args);
  }

  public V invokeStatic(Object... args) {
    return invoke(null, null, args);
  }

  @Override
  protected Method lookup() throws Exception {
    Objects.requireNonNull(parameters);
    for (State state : State.values()) {
      String n = name.getByState(state);
      if (!Strings.isNullOrEmpty(n))
        try {
          Method m = insideClass.getDeclaredMethod(n, parameters);
          m.setAccessible(true);
          return m;
        } catch (Exception e) {;
        }
    }
    return null;
  }
}
