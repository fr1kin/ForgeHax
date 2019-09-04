package com.matt.forgehax.asm.utils.environment;

import javax.annotation.Nullable;

/**
 * Created on 5/28/2017 by fr1kin
 */
public interface IStateMapper {
  
  @Nullable
  String getObfClassName(String className);
  
  @Nullable
  String getMcpClassName(String className);
  
  @Nullable
  String getSrgMethodName(String parentClassName, String methodName, String methodDescriptor);
  
  @Nullable
  String getObfMethodName(String parentClassName, String methodName, String methodDescriptor);
  
  @Nullable
  String getSrgFieldName(String parentClassName, String fieldName);
  
  @Nullable
  String getObfFieldName(String parentClassName, String fieldName);
}
