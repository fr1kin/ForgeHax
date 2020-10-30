package dev.fiki.forgehax.main.util.cmd.value;

import dev.fiki.forgehax.main.util.cmd.argument.IArgument;

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
