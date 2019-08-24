package com.matt.forgehax.asm.utils.asmtype;

import javax.annotation.Nullable;

/**
 * Created on 5/26/2017 by fr1kin
 */
public abstract class ASMClassChild implements IASMType {
  
  private final ASMClass parentClass;
  
  public ASMClassChild(@Nullable ASMClass parentClass) {
    this.parentClass = parentClass;
  }
  
  /**
   * The parent class to this child element
   *
   * @return null if no parent is specified - which is allowed.
   */
  @Nullable
  public ASMClass getParentClass() {
    return parentClass;
  }
}
