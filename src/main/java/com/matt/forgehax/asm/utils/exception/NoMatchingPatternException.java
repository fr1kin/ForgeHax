package com.matt.forgehax.asm.utils.exception;

/** Created on 5/4/2017 by fr1kin */
public class NoMatchingPatternException extends RuntimeException {
  public NoMatchingPatternException() {
    super();
  }

  public NoMatchingPatternException(String message) {
    super(message);
  }

  public NoMatchingPatternException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoMatchingPatternException(Throwable cause) {
    super(cause);
  }

  protected NoMatchingPatternException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
