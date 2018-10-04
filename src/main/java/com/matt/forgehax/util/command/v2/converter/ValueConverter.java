package com.matt.forgehax.util.command.v2.converter;

import com.matt.forgehax.util.command.v2.converter.exceptions.ValueParseException;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public interface ValueConverter<E> {

  /**
   * Find the value of a string with a given stream of data
   *
   * @param input string to parse
   * @param stream stream of data to filter
   * @return the most accurate match
   * @throws ValueParseException
   */
  E valueOf(String input, Stream<E> stream) throws ValueParseException;

  /**
   * Find the most probable value of the given input
   *
   * @param input string to parse
   * @return null if no possible interpretation
   * @throws ValueParseException if there was an issue parsing the input
   */
  @Nullable
  default E valueOf(String input) throws ValueParseException {
    return valueOf(input, Stream.empty());
  }

  /**
   * Same as other method, but allows a default value to be passed which will be used if
   * ValueParseException is thrown
   *
   * @param input string to parse
   * @param defaultTo value to default too
   * @return default value if ValueParseException is thrown
   */
  default E valueOf(String input, @Nullable E defaultTo) {
    try {
      return valueOf(input);
    } catch (ValueParseException e) {
      return defaultTo;
    }
  }
}
