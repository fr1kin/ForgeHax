package dev.fiki.forgehax.api.cmd.settings;

import dev.fiki.forgehax.api.cmd.AbstractSetting;
import dev.fiki.forgehax.api.cmd.IParentCommand;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;

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

  public abstract int intValue();

  public abstract long longValue();

  public abstract float floatValue();

  public abstract double doubleValue();

  public abstract byte byteValue();

  public abstract short shortValue();
}
