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

public final class IntegerSetting extends NumberSetting<Integer> {
  @Builder
  public IntegerSetting(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags, @Singular List<ISettingValueChanged<Integer>> changedListeners,
      @NonNull Number defaultTo, Number min, Number max) {
    super(parent, name, aliases, description, flags, defaultTo, min, max, Number::intValue);
    addListeners(ISettingValueChanged.class, changedListeners);
    onFullyConstructed();
  }

  @Override
  public int intValue() {
    return getValue();
  }

  @Override
  public IConverter<Integer> getConverter() {
    return TypeConverters.INTEGER;
  }
}
