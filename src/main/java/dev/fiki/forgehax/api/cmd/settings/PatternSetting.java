package dev.fiki.forgehax.api.cmd.settings;

import dev.fiki.forgehax.api.cmd.AbstractSetting;
import dev.fiki.forgehax.api.cmd.IParentCommand;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.typeconverter.IConverter;
import dev.fiki.forgehax.api.typeconverter.TypeConverters;
import lombok.Builder;

import java.util.Collection;
import java.util.regex.Pattern;

public final class PatternSetting extends AbstractSetting<Pattern> {
  @Builder
  public PatternSetting(IParentCommand parent,
      String name, Collection<String> aliases, String description,
      Collection<EnumFlag> flags,
      Pattern defaultTo) {
    super(parent, name, aliases, description, flags, defaultTo, null, null);
    onFullyConstructed();
  }

  @Override
  public IConverter<Pattern> getConverter() {
    return TypeConverters.PATTERN;
  }
}
