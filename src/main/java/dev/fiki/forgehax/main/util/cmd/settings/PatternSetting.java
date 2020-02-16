package dev.fiki.forgehax.main.util.cmd.settings;

import dev.fiki.forgehax.main.util.cmd.AbstractSetting;
import dev.fiki.forgehax.main.util.cmd.IParentCommand;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.typeconverter.IConverter;
import dev.fiki.forgehax.main.util.typeconverter.TypeConverters;
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
