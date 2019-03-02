package com.matt.forgehax.asm.utils.fasttype;

import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.utils.environment.RuntimeState;
import com.matt.forgehax.asm.utils.name.NameBuilder;
import java.util.Arrays;
import java.util.Objects;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.objectweb.asm.Type;

/** Created on 5/25/2017 by fr1kin */
public class FastTypeBuilder implements ASMCommon {
  public static FastTypeBuilder create() {
    return new FastTypeBuilder();
  }

  private Class<?> insideClass = null;
  private String name = null, srgName = null;

  // method only
  private Class<?>[] parameters = null;
  private Class<?> returnType = null;

  private boolean auto = false;
  private boolean stripFinal = false;

  public FastTypeBuilder setInsideClass(Class<?> insideClass) {
    this.insideClass = insideClass;
    return this;
  }

  public FastTypeBuilder setInsideClass(FastClass clazz) {
    return setInsideClass(clazz.getClassHandle());
  }

  public FastTypeBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public FastTypeBuilder setSrgName(String name) {
    this.srgName = name;
    return this;
  }

  public FastTypeBuilder setParameters(Class<?>... parameters) {
    this.parameters = Arrays.copyOf(parameters, parameters.length);
    return this;
  }

  /**
   * Only required if you want to use autoAssign() on a method
   *
   * @param returnType
   */
  public FastTypeBuilder setReturnType(Class<?> returnType) {
    this.returnType = returnType;
    return this;
  }

  public FastTypeBuilder autoAssign() {
    auto = true;
    return this;
  }

  public FastTypeBuilder definalize() {
    this.stripFinal = true;
    return this;
  }

  public FastClass asClass() {
    Objects.requireNonNull(name);
    srgName = name;
    return new FastClass(NameBuilder.create(name, srgName));
  }

  public <V> FastField<V> asField() {
    Objects.requireNonNull(insideClass);
    Objects.requireNonNull(name);
    if (auto && RuntimeState.isSrg()) {
      String parentClassInternalName = Type.getType(insideClass).getInternalName();
      srgName = MAPPER.getSrgFieldName(parentClassInternalName, name);
    }
    return new FastField<V>(
        insideClass, NameBuilder.create(name, srgName), stripFinal);
  }

  public <V> FastMethod<V> asMethod() {
    Objects.requireNonNull(insideClass);
    Objects.requireNonNull(name);
    Objects.requireNonNull(parameters);
    if (auto) {
      Objects.requireNonNull(returnType, "Return type required for auto assigning methods");
      String parentClassInternalName = Type.getType(insideClass).getInternalName();
      // build method descriptor
      Type[] args = new Type[parameters.length];
      for (int i = 0; i < args.length; i++) args[i] = Type.getType(parameters[i]);
      String descriptor = Type.getMethodType(Type.getType(returnType), args).getDescriptor();
      srgName = MAPPER.getSrgMethodName(parentClassInternalName, name, descriptor);
    }
    return new FastMethod<V>(
        insideClass, NameBuilder.create(name, srgName), parameters);
  }
}
