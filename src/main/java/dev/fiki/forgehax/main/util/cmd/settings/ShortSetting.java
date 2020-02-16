package dev.fiki.forgehax.main.util.cmd.settings;

import dev.fiki.forgehax.main.util.cmd.AbstractSetting;
import dev.fiki.forgehax.main.util.cmd.IParentCommand;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.typeconverter.IConverter;
import dev.fiki.forgehax.main.util.typeconverter.TypeConverters;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class ShortSetting extends AbstractSetting<Short> {
  @Builder
  public ShortSetting(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags, @Singular List<ISettingValueChanged<Short>> changedListeners,
      @NonNull Short defaultTo, Short min, Short max) {
    super(parent, name, aliases, description, flags, defaultTo, min, max);
    addListeners(ISettingValueChanged.class, changedListeners);
    onFullyConstructed();
  }

  @Override
  public IConverter<Short> getConverter() {
    return TypeConverters.SHORT;
  }
}
