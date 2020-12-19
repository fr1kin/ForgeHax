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

public final class FloatSetting extends NumberSetting<Float> {
  @Builder
  public FloatSetting(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags, @Singular List<ISettingValueChanged<Float>> changedListeners,
      @NonNull Number defaultTo, Number min, Number max) {
    super(parent, name, aliases, description, flags, defaultTo, min, max, Number::floatValue);
    addListeners(ISettingValueChanged.class, changedListeners);
    onFullyConstructed();
  }

  @Override
  public int intValue() {
    return (int) floatValue();
  }

  @Override
  public long longValue() {
    return (long) floatValue();
  }

  @Override
  public float floatValue() {
    return getValue();
  }

  @Override
  public double doubleValue() {
    return floatValue();
  }

  @Override
  public byte byteValue() {
    return (byte) floatValue();
  }

  @Override
  public short shortValue() {
    return (short) floatValue();
  }

  @Override
  public IConverter<Float> getConverter() {
    return TypeConverters.FLOAT;
  }
}
