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

public final class ByteSetting extends NumberSetting<Byte> {
  @Builder
  public ByteSetting(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags, @Singular List<ISettingValueChanged<Byte>> changedListeners,
      @NonNull Number defaultTo, Number min, Number max) {
    super(parent, name, aliases, description, flags, defaultTo, min, max, Number::byteValue);
    addListeners(ISettingValueChanged.class, changedListeners);
    onFullyConstructed();
  }

  @Override
  public int intValue() {
    return byteValue();
  }

  @Override
  public long longValue() {
    return byteValue();
  }

  @Override
  public float floatValue() {
    return byteValue();
  }

  @Override
  public double doubleValue() {
    return byteValue();
  }

  @Override
  public byte byteValue() {
    return getValue();
  }

  @Override
  public short shortValue() {
    return byteValue();
  }

  @Override
  public IConverter<Byte> getConverter() {
    return TypeConverters.BYTE;
  }
}
