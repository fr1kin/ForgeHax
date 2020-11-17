package dev.fiki.forgehax.api.cmd.value;

import dev.fiki.forgehax.api.cmd.argument.IArgument;

import java.util.Optional;

public interface IValue<E> {
  E getValue();

  default String getStringValue() {
    return getConverter().convert(getValue());
  }

  IArgument<E> getConverter();

  default E getDefaultValue() {
    return getConverter().getDefaultValue();
  }

  default Optional<E> getOptionalValue() {
    return Optional.ofNullable(getValue());
  }
}
