package com.matt.forgehax.util.command.v2.argument;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

public interface ValueMap<E> {
  /**
   * Get a stream of values from the input map with the given filter
   *
   * @param filter filter to apply
   * @return stream of filtered values
   */
  Stream<E> values(int index, Stream<E> filter);

  /**
   * Get a stream of values from the input map.
   *
   * @return stream of values
   */
  default Stream<E> values(int index) {
    return values(index, Stream.empty());
  }

  /**
   * Get the most accurate value from a given filter
   *
   * @param filter filter for the data
   * @return most accurate value from the input
   */
  default E value(int index, Stream<E> filter) {
    return values(index, filter).findFirst().orElse(null);
  }

  /**
   * Get the most accurate value.
   *
   * @return most accurate value.
   */
  default E value(int index) {
    return value(index, Stream.empty());
  }

  default Stream<E> firstValues(Stream<E> filter) {
    return values(0, filter);
  }

  default Stream<E> firstValues() {
    return firstValues(Stream.empty());
  }

  default E firstValue(Stream<E> filter) {
    return firstValues(filter).findFirst().orElse(null);
  }

  default E firstValue() {
    return firstValue(Stream.empty());
  }

  /**
   * Gets a collection of current inputs inserted into this map
   *
   * @return collection of inputs
   */
  Collection<String> getInputs();

  /**
   * Get the size of the inputs collection
   *
   * @return number of inputs
   */
  int getInputSize();

  default String getInput(int index) {
    return null; // Utils.getOrDefault(getInputs(), index, null); // TODO
  }

  /**
   * Adds an iterable object to the input collection
   *
   * @param inputs iterable object to insert
   */
  void withInputs(Iterable<String> inputs);

  /**
   * Adds a single object to the input collection
   *
   * @param input single string object
   */
  default void withInput(String input) {
    Objects.requireNonNull(input);
    withInputs(Collections.singleton(input));
  }

  void withDefaultValue();
}
