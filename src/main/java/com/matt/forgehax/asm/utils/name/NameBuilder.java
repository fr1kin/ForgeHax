package com.matt.forgehax.asm.utils.name;

/** Created on 5/27/2017 by fr1kin */
public class NameBuilder {
  public static <E> IName<E> create(E real, E srg) {
    if (srg == null) return createSingleName(real);
    else return createMcMultiName(real, srg);
  }

  public static <E> IName<E> createSingleName(E real) {
    return new SingleName<E>(real);
  }

  public static <E> IName<E> createMcMultiName(E real, E srg) {
    return new McMultiName<E>(real, srg);
  }
}
