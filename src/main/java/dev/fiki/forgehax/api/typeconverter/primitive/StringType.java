package dev.fiki.forgehax.api.typeconverter.primitive;

import dev.fiki.forgehax.api.typeconverter.TypeConverter;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * Created on 3/23/2017 by fr1kin
 */
public class StringType extends TypeConverter<String> {
  
  @Override
  public String label() {
    return "string";
  }
  
  @Override
  public Class<String> type() {
    return String.class;
  }
  
  @Override
  public String parse(String value) {
    return value;
  }
  
  @Override
  public String convert(String value) {
    return value;
  }
  
  @Nullable
  @Override
  public Comparator<String> comparator() {
    return String::compareTo;
  }

  public static class CaseInsensitive extends StringType {
    @Override
    public String label() {
      return "string_case_insensitive";
    }

    @Nullable
    @Override
    public Comparator<String> comparator() {
      return String::compareToIgnoreCase;
    }
  }
}
