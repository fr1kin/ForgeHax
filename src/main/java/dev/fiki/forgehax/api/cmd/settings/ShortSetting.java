package dev.fiki.forgehax.api.cmd.settings;

import dev.fiki.forgehax.api.cmd.IParentCommand;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.typeconverter.IConverter;
import dev.fiki.forgehax.api.typeconverter.TypeConverters;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;
import java.util.Set;

public final class ShortSetting extends NumberSetting<Short> {
  @Builder
  public ShortSetting(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags, @Singular List<ISettingValueChanged<Short>> changedListeners,
      @NonNull Number defaultTo, Number min, Number max) {
    super(parent, name, aliases, description, flags, defaultTo, min, max, Number::shortValue);
    addListeners(ISettingValueChanged.class, changedListeners);
    onFullyConstructed();
  }

  @Override
  public int intValue() {
    return shortValue();
  }

  @Override
  public long longValue() {
    return shortValue();
  }

  @Override
  public float floatValue() {
    return shortValue();
  }

  @Override
  public double doubleValue() {
    return shortValue();
  }

  @Override
  public byte byteValue() {
    return (byte) shortValue();
  }

  @Override
  public short shortValue() {
    return getValue();
  }

  @Override
  public IConverter<Short> getConverter() {
    return TypeConverters.SHORT;
  }
}
