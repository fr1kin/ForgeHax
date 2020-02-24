package dev.fiki.forgehax.main.util.cmd.settings;

import dev.fiki.forgehax.main.util.cmd.AbstractSetting;
import dev.fiki.forgehax.main.util.cmd.IParentCommand;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;

import java.util.Collection;
import java.util.function.Function;

abstract class NumberSetting<E extends Number> extends AbstractSetting<E> {
  public NumberSetting(IParentCommand parent,
      String name, Collection<String> aliases, String description,
      Collection<EnumFlag> flags,
      Number defaultTo, Number min, Number max, Function<Number, E> numberConverter) {
    super(parent, name, aliases, description, flags,
        defaultTo == null ? null : numberConverter.apply(defaultTo),
        min == null ? null : numberConverter.apply(min),
        max == null ? null : numberConverter.apply(max));
  }

  public int intValue() {
    return (int) getValue();
  }

  public long longValue() {
    return (long) getValue();
  }

  public float floatValue() {
    return (float) getValue();
  }

  public double doubleValue() {
    return (double) getValue();
  }

  public byte byteValue() {
    return (byte) getValue();
  }

  public short shortValue() {
    return (short) getValue();
  }
}
