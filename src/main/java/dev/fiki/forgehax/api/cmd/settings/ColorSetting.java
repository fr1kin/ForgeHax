package dev.fiki.forgehax.api.cmd.settings;

import dev.fiki.forgehax.api.cmd.AbstractSetting;
import dev.fiki.forgehax.api.cmd.IParentCommand;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.color.Color;
import dev.fiki.forgehax.api.typeconverter.IConverter;
import dev.fiki.forgehax.api.typeconverter.TypeConverters;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;
import java.util.Set;

public final class ColorSetting extends AbstractSetting<Color> {
  @Builder
  public ColorSetting(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags, @Singular List<ISettingValueChanged<Color>> changedListeners,
      @NonNull Color defaultTo) {
    super(parent, name, aliases, description, flags, defaultTo, null, null);
    addListeners(ISettingValueChanged.class, changedListeners);
    onFullyConstructed();
  }

  @Override
  public IConverter<Color> getConverter() {
    return TypeConverters.COLOR;
  }

  @Override
  protected int getMaxArguments() {
    return 4;
  }

  public String getColorName() {
    return getValue().getName();
  }
}
