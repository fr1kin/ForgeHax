package dev.fiki.forgehax.api.cmd.settings;

import dev.fiki.forgehax.api.cmd.AbstractSetting;
import dev.fiki.forgehax.api.cmd.IParentCommand;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.typeconverter.IConverter;
import dev.fiki.forgehax.api.typeconverter.TypeConverters;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;
import java.util.Set;

public final class BooleanSetting extends AbstractSetting<Boolean> {
  @Builder
  public BooleanSetting(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags, @Singular List<ISettingValueChanged<Boolean>> changedListeners,
      @NonNull Boolean defaultTo) {
    super(parent, name, aliases, description, flags, defaultTo, Boolean.FALSE, Boolean.TRUE);
    addListeners(ISettingValueChanged.class, changedListeners);
    onFullyConstructed();
  }

  @Override
  public IConverter<Boolean> getConverter() {
    return TypeConverters.BOOLEAN;
  }

  public boolean isEnabled() {
    return getValue();
  }

  public boolean isDisabled() {
    return !getValue();
  }

  public boolean enable() {
    return setValue(true);
  }

  public boolean disable() {
    return setValue(false);
  }

  public boolean toggle() {
    return setValue(!getValue());
  }
}
