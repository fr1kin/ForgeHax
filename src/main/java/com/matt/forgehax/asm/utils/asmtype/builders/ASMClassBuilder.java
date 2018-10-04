package com.matt.forgehax.asm.utils.asmtype.builders;

import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.utils.asmtype.ASMClass;
import com.matt.forgehax.asm.utils.name.NameBuilder;
import java.util.Objects;
import joptsimple.internal.Strings;
import org.objectweb.asm.Type;

/** Created on 5/27/2017 by fr1kin */
public class ASMClassBuilder implements ASMCommon {
  private Type name = null, srgName = null, obfuscatedName = null;

  private boolean auto = false;

  protected ASMClassBuilder() {}

  public ASMClassBuilder setClassName(Type type) {
    name = type;
    return this;
  }

  public ASMClassBuilder setClassName(String internalClassName) {
    return setClassName(
        !Strings.isNullOrEmpty(internalClassName) ? Type.getObjectType(internalClassName) : null);
  }

  public ASMClassBuilder setClassName(Class<?> clazz) {
    return setClassName(Type.getType(clazz));
  }

  public ASMClassBuilder setSrgClassName(String srgInternalClassName) {
    srgName =
        !Strings.isNullOrEmpty(srgInternalClassName)
            ? Type.getObjectType(srgInternalClassName)
            : null;
    return this;
  }

  public ASMClassBuilder setObfuscatedClassName(String obfuscatedInternalClassName) {
    obfuscatedName =
        !Strings.isNullOrEmpty(obfuscatedInternalClassName)
            ? Type.getObjectType(obfuscatedInternalClassName)
            : null;
    return this;
  }

  public ASMClassBuilder autoAssign() {
    auto = true;
    return this;
  }

  private void attemptAutoAssign() {
    // srg name = mcp name (normal state) so no need to set it
    setObfuscatedClassName(MAPPER.getObfClassName(name.getInternalName()));
  }

  public ASMClass build() {
    Objects.requireNonNull(name, "Class name is missing");
    if (auto) attemptAutoAssign();
    return new ASMClass(NameBuilder.create(name, srgName, obfuscatedName));
  }
}
