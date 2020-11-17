package dev.fiki.forgehax.api.cmd.argument;

import dev.fiki.forgehax.api.typeconverter.IConverter;

public interface IArgument<E> extends IConverter<E> {
  String getLabel();

  E getDefaultValue();

  int getMinArgumentsConsumed();
  int getMaxArgumentsConsumed();

  default boolean isOptional() {
    return getMinArgumentsConsumed() <= 0;
  }
}
