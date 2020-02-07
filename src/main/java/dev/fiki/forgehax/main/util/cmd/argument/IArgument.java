package dev.fiki.forgehax.main.util.cmd.argument;

import dev.fiki.forgehax.main.util.typeconverter.IConverter;

public interface IArgument<E> extends IConverter<E> {
  String getLabel();

  E getDefaultValue();

  int getMinArgumentsConsumed();
  int getMaxArgumentsConsumed();

  default boolean isOptional() {
    return getMinArgumentsConsumed() <= 0;
  }
}
