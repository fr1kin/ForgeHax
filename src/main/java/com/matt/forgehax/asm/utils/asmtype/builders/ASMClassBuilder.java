package com.matt.forgehax.asm.utils.asmtype.builders;

import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.utils.asmtype.ASMClass;
import com.matt.forgehax.asm.utils.name.NameBuilder;
import java.util.Objects;
import joptsimple.internal.Strings;
import org.objectweb.asm.Type;

/** Created on 5/27/2017 by fr1kin */
public class ASMClassBuilder implements ASMCommon {
  private Type name = null;

  protected ASMClassBuilder() {}

  public ASMClassBuilder setClassName(Type type) {
    name = type;
    return this;
  }

  public ASMClassBuilder setClassName(String internalClassName) {
    return setClassName(Type.getObjectType(internalClassName));
  }

  public ASMClassBuilder setClassName(Class<?> clazz) {
    return setClassName(Type.getType(clazz));
  }


  public ASMClass build() {
    Objects.requireNonNull(name, "Class name is missing");
    return new ASMClass(NameBuilder.createSingleName(name));
  }
}
