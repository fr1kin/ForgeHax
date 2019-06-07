package com.matt.forgehax.asm.utils.name;

/** Created on 5/27/2017 by fr1kin */
public class NameBuilder {
  @Deprecated
  public static <E> IName<E> create(E real, E srg) {
    if (srg == null) return createSingleName(real);
    else return createMultiName(real, srg);
  }

  public static <E> IName<E> createSingleName(E real) {
    return new SingleName<E>(real);
  }

  public static <E> IName<E> createMultiName(E real, E srg) {
    return new MultiName<E>(real, srg);
  }
}
