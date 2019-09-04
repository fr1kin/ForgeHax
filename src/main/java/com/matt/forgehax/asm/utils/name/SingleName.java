package com.matt.forgehax.asm.utils.name;

import com.matt.forgehax.asm.utils.environment.State;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 5/26/2017 by fr1kin
 */
public class SingleName<E> implements IName<E> {
  
  private final E normal;

  public SingleName(@Nonnull E normal) {
    Objects.requireNonNull(normal);
    this.normal = normal;
  }

  @Override
  public E get() {
    return normal;
  }

  @Nullable
  @Override
  public E getByState(State state) {
    switch (state) {
      case NORMAL:
        return get();
      default:
        return null;
    }
  }

  @Override
  public int getStateCount() {
    return 1;
  }
}
