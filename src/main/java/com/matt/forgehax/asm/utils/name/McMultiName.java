package com.matt.forgehax.asm.utils.name;

import com.matt.forgehax.asm.utils.environment.State;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 5/26/2017 by fr1kin
 */
public class McMultiName<E> extends SingleName<E> {
  
  private final E srg;
  private final E obf;
  
  public McMultiName(@Nonnull E type, @Nullable E srg, @Nullable E obf) {
    super(type);
    this.srg = srg;
    this.obf = obf;
  }
  
  @Nullable
  public E getSrg() {
    return srg;
  }
  
  @Nullable
  public E getObf() {
    return obf;
  }
  
  @Nullable
  @Override
  public E getByState(State state) {
    switch (state) {
      case SRG:
        return srg;
      case OBFUSCATED:
        return obf;
      case NORMAL:
        return get();
      default:
        return null;
    }
  }
  
  @Override
  public int getStateCount() {
    return super.getStateCount() + (srg != null ? 1 : 0) + (obf != null ? 1 : 0);
  }
}
