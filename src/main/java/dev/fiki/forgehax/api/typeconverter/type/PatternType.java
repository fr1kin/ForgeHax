package dev.fiki.forgehax.api.typeconverter.type;

import dev.fiki.forgehax.api.typeconverter.TypeConverter;

import java.util.regex.Pattern;

public class PatternType extends TypeConverter<Pattern> {
  @Override
  public String label() {
    return "regex";
  }

  @Override
  public Class<Pattern> type() {
    return Pattern.class;
  }

  @Override
  public Pattern parse(String value) {
    return Pattern.compile(value);
  }

  @Override
  public String convert(Pattern value) {
    return value.pattern();
  }
}
