package com.matt.forgehax.asm.utils.asmtype.builders;

import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.utils.asmtype.ASMClass;
import com.matt.forgehax.asm.utils.asmtype.ASMField;
import com.matt.forgehax.asm.utils.name.IName;
import com.matt.forgehax.asm.utils.name.NameBuilder;
import java.util.Objects;
import joptsimple.internal.Strings;
import org.objectweb.asm.Type;

/**
 * Created on 5/27/2017 by fr1kin
 */
public class ASMFieldBuilder implements ASMCommon {
  
  private ASMClass parentClass = null;
  private String name = null, srgName = null, obfuscatedName = null;
  private IName<Type> type = null;
  
  private boolean auto = false;
  
  protected ASMFieldBuilder() {
  }
  
  public ASMFieldBuilder setParentClass(ASMClass parentClass) {
    this.parentClass = parentClass;
    return this;
  }
  
  public ASMFieldBuilder setParentClass(Type type) {
    return setParentClass(ASMBuilders.newClassBuilder().setClassName(type).build());
  }
  
  public ASMFieldBuilder setParentClass(String internalClassName) {
    return setParentClass(ASMBuilders.newClassBuilder().setClassName(internalClassName).build());
  }
  
  public ASMFieldBuilder setParentClass(Class<?> clazz) {
    return setParentClass(ASMBuilders.newClassBuilder().setClassName(clazz).build());
  }
  
  public ASMFieldBuilder setName(String name) {
    this.name = name;
    return this;
  }
  
  public ASMFieldBuilder setSrgName(String srgName) {
    this.srgName = srgName;
    return this;
  }
  
  public ASMFieldBuilder setObfuscatedName(String obfuscatedName) {
    this.obfuscatedName = obfuscatedName;
    return this;
  }
  
  public ASMFieldBuilder setType(IName<Type> type) {
    this.type = type;
    return this;
  }
  
  public ASMFieldBuilder setType(Type type) {
    return setType(NameBuilder.createSingleName(type));
  }
  
  public ASMFieldBuilder setType(String internalClassName) {
    return setType(
      !Strings.isNullOrEmpty(internalClassName) ? Type.getObjectType(internalClassName) : null);
  }
  
  public ASMFieldBuilder setType(Class<?> clazz) {
    return setType(Type.getType(clazz));
  }
  
  public ASMFieldBuilder setType(ASMClass clazz) {
    return setType(clazz.getAll());
  }
  
  public ASMFieldBuilder autoAssign() {
    auto = true;
    return this;
  }
  
  private void attemptAutoAssign() {
    setSrgName(MAPPER.getSrgFieldName(parentClass.getInternalName(), name));
    setObfuscatedName(MAPPER.getObfFieldName(parentClass.getInternalName(), name));
  }
  
  public ASMField build() {
    Objects.requireNonNull(name, "Missing field name");
    Objects.requireNonNull(type, "Missing field type");
    if (auto) {
      attemptAutoAssign();
    }
    return new ASMField(parentClass, NameBuilder.create(name, srgName, obfuscatedName), type);
  }
}
