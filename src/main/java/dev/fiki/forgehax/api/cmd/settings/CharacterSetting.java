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

public final class CharacterSetting extends AbstractSetting<Character> implements CharSequence {
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

  @Override
  public int length() {
    return 1;
  }

  @Override
  public char charAt(int index) {
    if (index == 0) {
      return getValue();
    } else {
      throw new IndexOutOfBoundsException("Tried to get character at index #" + index);
    }
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    if (start == 0 && end == 1) {
      return String.valueOf(getValue());
    } else {
      throw new IndexOutOfBoundsException("Cannot copy sequence index " + start + " to " + end);
    }
  }
}
