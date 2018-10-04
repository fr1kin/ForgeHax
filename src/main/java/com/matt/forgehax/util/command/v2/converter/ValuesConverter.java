package com.matt.forgehax.util.command.v2.converter;

import com.matt.forgehax.util.command.v2.converter.exceptions.ValueParseException;
import java.util.stream.Stream;

public interface ValuesConverter<E> {
  /**
   * Get a list of possible values for the given text
   *
   * @param input string to parse
   * @param stream stream of data to filter
   * @return collection sorted from most likely to least likely
   */
  Stream<E> valuesOf(String input, Stream<E> stream) throws ValueParseException;

  /**
   * Get a list of possible values for the given text.
   *
   * @param input string to parse
   * @return collection of valid data
   * @throws ValueParseException if the parse fails
   */
  default Stream<E> valuesOf(String input) throws ValueParseException {
    return valuesOf(input, Stream.empty());
  }
}
