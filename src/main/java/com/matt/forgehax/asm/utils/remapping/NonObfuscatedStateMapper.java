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
  public String getObfClassName(String className) {
    return null;
  }

  @Nullable
  @Override
  public String getMcpClassName(String className) {
    return null;
  }

  @Nullable
  @Override
  public String getSrgMethodName(
      String parentClassName, String methodName, String methodDescriptor) {
    return ObfuscatedStateMapper.getInstance()
        .getSrgMethodName(parentClassName, methodName, methodDescriptor);
  }

  @Nullable
  @Override
  public String getObfMethodName(
      String parentClassName, String methodName, String methodDescriptor) {
    return null;
  }

  @Nullable
  @Override
  public String getSrgFieldName(String parentClassName, String fieldName) {
    return ObfuscatedStateMapper.getInstance().getSrgFieldName(parentClassName, fieldName);
  }

  @Nullable
  @Override
  public String getObfFieldName(String parentClassName, String fieldName) {
    return null;
  }
}
