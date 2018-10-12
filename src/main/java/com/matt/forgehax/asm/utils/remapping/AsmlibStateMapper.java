package com.matt.forgehax.asm.utils.remapping;

import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.utils.environment.IStateMapper;
import javax.annotation.Nullable;
import net.futureclient.asm.obfuscation.ObfuscatedRemapper;

public class AsmlibStateMapper implements ASMCommon, IStateMapper {

  private static final AsmlibStateMapper INSTANCE = new AsmlibStateMapper();

  public static AsmlibStateMapper getInstance() {
    return INSTANCE;
  }

  @Nullable
  @Override
  public String getObfClassName(String className) {
    return ObfuscatedRemapper.getInstance().getClassName(className);
  }

  @Nullable
  @Override
  public String getMcpClassName(String className) {
    return ObfuscatedRemapper.getInstance().getMcpClassName(className);
  }

  @Nullable
  @Override
  public String getSrgMethodName(String parentClassName, String methodName,
      String methodDescriptor) {
    // TODO: implement
    return null;
  }

  @Nullable
  @Override
  public String getObfMethodName(String parentClassName, String methodName,
      String methodDescriptor) {
    return ObfuscatedRemapper.getInstance().getMethodName(parentClassName, methodName, methodDescriptor);
  }

  @Nullable
  @Override
  public String getSrgFieldName(String parentClassName, String fieldName) {
    return null;
  }

  @Nullable
  @Override
  public String getObfFieldName(String parentClassName, String fieldName) {
    return ObfuscatedRemapper.getInstance().getFieldName(parentClassName, fieldName);
  }
}
