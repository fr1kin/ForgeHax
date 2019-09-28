package com.matt.forgehax.asm.utils.asmtype.builders;

import com.google.common.collect.Lists;
import com.matt.forgehax.asm.utils.asmtype.ASMClass;
import com.matt.forgehax.asm.utils.name.IName;
import com.matt.forgehax.asm.utils.name.NameBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import joptsimple.internal.Strings;
import org.objectweb.asm.Type;

/**
 * Created on 5/27/2017 by fr1kin
 */
public class ParameterBuilder {
  
  private ASMMethodBuilder callback = null;
  private List<IName<Type>> parameters = Lists.newArrayList();
  private boolean overrideObfuscation = false;
  
  protected ParameterBuilder() {
  }
  
  protected ParameterBuilder(ASMMethodBuilder callback) {
    this.callback = callback;
  }
  
  // Override runtime obfuscation state so
  // every type is treated as unobfuscated
  public ParameterBuilder unobfuscated() {
    overrideObfuscation = true;
    return this;
  }
  
  public ParameterBuilder add(Type parameter) {
    parameters.add(NameBuilder.createSingleName(parameter));
    return this;
  }
  
  public ParameterBuilder add(String internalClassName) {
    return add(
      !Strings.isNullOrEmpty(internalClassName) ? Type.getObjectType(internalClassName) : null);
  }
  
  public ParameterBuilder add(Class<?> clazz) {
    return add(Type.getType(clazz));
  }
  
  public ParameterBuilder add(ASMClass parameter) {
    if (overrideObfuscation) {
      parameters.add(NameBuilder.createSingleName(parameter.getAll().get()));
    } else {
      parameters.add(parameter.getAll());
    }
    return this;
  }
  
  public IName<Type>[] asArray() {
    return parameters.toArray(new IName[0]);
  }
  
  public Collection<IName<Type>> asCollection() {
    return Collections.unmodifiableCollection(parameters);
  }
  
  public ASMMethodBuilder finish() {
    Objects.requireNonNull(callback, "Attempted to use finishParameters() without a callback");
    return callback.setParameterTypes(asArray());
  }
}
