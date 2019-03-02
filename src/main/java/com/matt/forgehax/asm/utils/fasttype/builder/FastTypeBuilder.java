package com.matt.forgehax.asm.utils.fasttype.builder;

import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.utils.environment.RuntimeState;
import com.matt.forgehax.asm.utils.fasttype.FastClass;
import com.matt.forgehax.asm.utils.fasttype.FastField;
import com.matt.forgehax.asm.utils.fasttype.FastMethod;
import com.matt.forgehax.asm.utils.name.NameBuilder;
import java.util.Arrays;
import java.util.Objects;

import org.objectweb.asm.Type;

/** Created on 5/25/2017 by fr1kin */
public abstract class FastTypeBuilder<T extends FastTypeBuilder> implements ASMCommon {

  protected Class<?> insideClass = null;
  protected String name = null, srgName = null;

  protected boolean auto = false;

  @SuppressWarnings("unchecked")
  public T setInsideClass(Class<?> insideClass) {
    this.insideClass = insideClass;
    return (T) this;
  }

  public T setInsideClass(FastClass clazz) {
    return setInsideClass(clazz.getClassHandle());
  }

  @SuppressWarnings("unchecked")
  public T setName(String name) {
    this.name = name;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T setSrgName(String name) {
    this.srgName = name;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T autoAssign() {
    auto = true;
    return (T) this;
  }

  // unused?
  public FastClass asClass() {
    Objects.requireNonNull(name);
    srgName = name;
    return new FastClass(NameBuilder.create(name, srgName));
  }



}
