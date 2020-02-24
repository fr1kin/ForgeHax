package dev.fiki.forgehax.main.util.cmd.settings;

import dev.fiki.forgehax.main.util.cmd.IParentCommand;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.typeconverter.IConverter;
import dev.fiki.forgehax.main.util.typeconverter.TypeConverters;
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
  public short shortValue() {
    return getValue();
  }

  @Override
  public IConverter<Short> getConverter() {
    return TypeConverters.SHORT;
  }
}
