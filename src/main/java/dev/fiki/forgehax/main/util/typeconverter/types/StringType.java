package dev.fiki.forgehax.main.util.typeconverter.types;

import dev.fiki.forgehax.main.util.typeconverter.TypeConverter;
import java.util.Comparator;
import javax.annotation.Nullable;

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
