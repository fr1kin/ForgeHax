package com.matt.forgehax.util.command.v2.converter.exceptions;

public class ValueParseException extends RuntimeException {
  public static void requireNonNull(Object o, String msg) throws ValueParseException {
    if (o == null) throw new ValueParseException(new NullPointerException(msg));
  }

  public static void requireNonNull(Object o) throws ValueParseException {
    requireNonNull(o, "null value");
  }

  public static void throwException(String message) throws ValueParseException {
    throw new ValueParseException(new Exception(message));
  }

  public ValueParseException(Throwable t) {
    super(t);
  }
}
