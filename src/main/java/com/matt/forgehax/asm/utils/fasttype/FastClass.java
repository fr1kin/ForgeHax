package com.matt.forgehax.asm.utils.fasttype;

import com.matt.forgehax.asm.utils.ASMStackLogger;
import com.matt.forgehax.asm.utils.environment.RuntimeState;
import com.matt.forgehax.asm.utils.name.IName;

/**
 * Created on 7/6/2017 by fr1kin
 */
public class FastClass extends FastType<Class<?>> {
  
  public FastClass(IName<String> name) {
    super(null, name);
  }
  
  public Class<?> getClassHandle() {
    try {
      if (attemptLookup()) {
        return type;
      }
    } catch (Throwable t) {
      if (printOnce.compareAndSet(false, true)) {
        ASMStackLogger.printStackTrace(t);
      }
    }
    return null;
  }
  
  @Override
  protected Class<?> lookup() throws Exception {
    return Class.forName(
      name.getByStateSafe(RuntimeState.getState()), false, getClass().getClassLoader());
  }
}
