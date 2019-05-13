package com.matt.forgehax.asm.utils.environment;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;

/** Created on 5/28/2017 by fr1kin */
public interface IStateMapper {


  @Nullable
  @Deprecated // might be implemented later
  String getSrgMethodName(String parentClassName, String methodName, String methodDescriptor);


  @Nullable
  @Deprecated // might me implemented later
  String getSrgFieldName(String parentClassName, String fieldName);
}
