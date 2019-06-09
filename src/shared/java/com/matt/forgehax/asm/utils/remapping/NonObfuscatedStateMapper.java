package com.matt.forgehax.asm.utils.remapping;

import com.matt.forgehax.asm.utils.environment.IStateMapper;

import javax.annotation.Nullable;

/** Created on 5/28/2017 by fr1kin */
public class NonObfuscatedStateMapper implements IStateMapper {
  private static NonObfuscatedStateMapper INSTANCE = null;

  public static NonObfuscatedStateMapper getInstance() {
    return INSTANCE == null ? INSTANCE = new NonObfuscatedStateMapper() : INSTANCE;
  }

  @Nullable
  @Override
  public String getSrgMethodName(String parentClassName, String methodName, String methodDescriptor) {
    throw new UnsupportedOperationException("Attempted to get srg name in non srg environment");
  }

  @Nullable
  @Override
  public String getSrgFieldName(String parentClassName, String fieldName) {
    throw new UnsupportedOperationException("Attempted to get srg name in non srg environment");
  }
}
