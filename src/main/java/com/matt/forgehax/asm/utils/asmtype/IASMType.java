package com.matt.forgehax.asm.utils.asmtype;

import com.matt.forgehax.asm.utils.environment.RuntimeState;
import com.matt.forgehax.asm.utils.environment.State;

/**
 * Created on 5/27/2017 by fr1kin
 */
public interface IASMType {
  
  String getNameByState(State state);
  
  String getDescriptorByState(State state);
  
  default String getName() {
    return getNameByState(State.NORMAL);
  }
  
  default String getDescriptor() {
    return getDescriptorByState(State.NORMAL);
  }
  
  default String getSrgName() {
    return getNameByState(State.SRG);
  }
  
  default String getSrgDescriptor() {
    return getDescriptorByState(State.SRG);
  }
  
  default String getObfuscatedName() {
    return getNameByState(State.OBFUSCATED);
  }
  
  default String getObfuscatedDescriptor() {
    return getDescriptorByState(State.OBFUSCATED);
  }
  
  default String getRuntimeName() {
    return getNameByState(RuntimeState.getState());
  }
  
  default String getRuntimeDescriptor() {
    return getDescriptorByState(RuntimeState.getState());
  }
}
