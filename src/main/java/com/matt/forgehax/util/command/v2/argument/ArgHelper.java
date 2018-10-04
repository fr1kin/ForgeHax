package com.matt.forgehax.util.command.v2.argument;

import com.matt.forgehax.util.command.v2.converter.DefaultConverters;
import java.util.Collection;

/** Created on 1/30/2018 by fr1kin */
public class ArgHelper {
  private static final IArg<Object> ARGUMENT_EMPTY =
      new ArgBuilder<Object>()
          .description("empty")
          .shortDescription("empty")
          .converter(DefaultConverters.EMPTY)
          .optional()
          .build();

  /**
   * Checks the required/non-required argument order to ensure that required argument(s) are not put
   * after a non-required argument.
   *
   * @param arguments Array of arguments
   * @return true if the order is wrong
   */
  public static boolean isInvalidArgumentOrder(Collection<IArg<?>> arguments) {
    boolean allowed = true;
    for (IArg<?> arg : arguments) {
      if (!arg.isRequired())
        allowed = false; // required arguments are not allowed after non-required arguments now
      else if (!allowed) // this is a required argument, if allowed=false then you have tried to
        // added a required argument after a non-required argument
        return true;
    }
    return false;
  }

  public static <T> IArg<T> emptyArgument() {
    return (IArg<T>) ARGUMENT_EMPTY;
  }
}
