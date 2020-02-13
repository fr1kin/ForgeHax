package dev.fiki.forgehax.main.util.cmd.settings;

import dev.fiki.forgehax.main.util.cmd.AbstractSetting;
import dev.fiki.forgehax.main.util.cmd.IParentCommand;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.color.Color;
import dev.fiki.forgehax.main.util.typeconverter.IConverter;
import dev.fiki.forgehax.main.util.typeconverter.TypeConverters;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;
import java.util.Set;

public class ColorSetting extends AbstractSetting<Color> {
  @Builder
  public ColorSetting(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags, @Singular List<ISettingValueChanged<Color>> changedListeners,
      @NonNull Color defaultTo) {
    super(parent, name, aliases, description, flags, defaultTo, null, null);
    addListeners(ISettingValueChanged.class, changedListeners);
  }

  @Override
  public IConverter<Color> getConverter() {
    return TypeConverters.COLOR;
  }

  @Override
  protected int getMaxArguments() {
    return 4;
  }
}
