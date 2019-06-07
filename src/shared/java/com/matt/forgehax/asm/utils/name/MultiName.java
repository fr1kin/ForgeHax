package com.matt.forgehax.asm.utils.name;

import com.matt.forgehax.asm.utils.environment.State;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Created on 5/26/2017 by fr1kin */
public class MultiName<E> extends SingleName<E> {
  private final E srg;

  public MultiName(@Nonnull E type, @Nullable E srg) {
    super(type);
    this.srg = srg;
  }

  @Nullable
  public E getSrg() {
    return srg;
  }


  @Nullable
  @Override
  public E getByState(State state) {
    switch (state) {
      case SRG:
        return srg;
      case NORMAL:
        return get();
      default:
        return null;
    }
  }

  @Override
  public int getStateCount() {
    return super.getStateCount() + (srg != null ? 1 : 0);
  }
}
