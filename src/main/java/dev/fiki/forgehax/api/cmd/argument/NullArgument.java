package dev.fiki.forgehax.api.cmd.argument;

import java.util.Comparator;

public class NullArgument<E> implements IArgument<E> {
  @Override
  public String getLabel() {
    return null;
  }

  @Override
  public E getDefaultValue() {
    return null;
  }

  @Override
  public int getMinArgumentsConsumed() {
    return 0;
  }

  @Override
  public int getMaxArgumentsConsumed() {
    return 0;
  }

  @Override
  public Class<E> type() {
    return null;
  }

  @Override
  public E parse(String value) {
    return null;
  }

  @Override
  public String convert(E value) {
    return null;
  }

  @Override
  public Comparator<E> comparator() {
    return null;
  }
}
