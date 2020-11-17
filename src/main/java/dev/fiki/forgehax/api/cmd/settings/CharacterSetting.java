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

public final class CharacterSetting extends AbstractSetting<Character> {
  @Builder
  public CharacterSetting(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags, @Singular List<ISettingValueChanged<Character>> changedListeners,
      @NonNull Character defaultTo, Character min, Character max) {
    super(parent, name, aliases, description, flags, defaultTo, min, max);
    addListeners(ISettingValueChanged.class, changedListeners);
    onFullyConstructed();
  }

  @Override
  public IConverter<Character> getConverter() {
    return TypeConverters.CHARACTER;
  }
}
