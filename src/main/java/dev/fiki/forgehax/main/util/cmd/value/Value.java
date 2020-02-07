package dev.fiki.forgehax.main.util.cmd.value;

import dev.fiki.forgehax.main.util.cmd.argument.IArgument;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Value<E> implements IValue<E> {
  private final E value;
  private final IArgument<E> converter;
}
