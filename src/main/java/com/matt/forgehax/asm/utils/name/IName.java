package com.matt.forgehax.asm.utils.name;

import com.matt.forgehax.asm.utils.environment.State;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 5/26/2017 by fr1kin
 */
public interface IName<E> {
  
  @Nonnull
  E get();
  
  @Nullable
  E getByState(State state);
  
  @Nonnull
  default E getByStateSafe(State state) {
    E value = getByState(state);
    return value != null ? value : get();
  }
  
  int getStateCount();
}
