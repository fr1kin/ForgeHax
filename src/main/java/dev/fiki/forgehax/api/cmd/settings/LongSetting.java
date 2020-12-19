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

public final class LongSetting extends NumberSetting<Long> {
  @Builder
  public LongSetting(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags, @Singular List<ISettingValueChanged<Long>> changedListeners,
      @NonNull Number defaultTo, Number min, Number max) {
    super(parent, name, aliases, description, flags, defaultTo, min, max, Number::longValue);
    addListeners(ISettingValueChanged.class, changedListeners);
    onFullyConstructed();
  }

  @Override
  public int intValue() {
    return (int) longValue();
  }

  @Override
  public long longValue() {
    return getValue();
  }

  @Override
  public float floatValue() {
    return longValue();
  }

  @Override
  public double doubleValue() {
    return longValue();
  }

  @Override
  public byte byteValue() {
    return (byte) longValue();
  }

  @Override
  public short shortValue() {
    return (short) longValue();
  }

  @Override
  public IConverter<Long> getConverter() {
    return TypeConverters.LONG;
  }
}
