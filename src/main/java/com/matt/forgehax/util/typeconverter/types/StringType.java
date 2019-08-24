package com.matt.forgehax.util.typeconverter.types;

import com.matt.forgehax.util.typeconverter.TypeConverter;
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
  public String toString(String value) {
    return value != null ? value : "null";
  }

  @Nullable
  @Override
  public Comparator<String> comparator() {
    return new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return o1.compareTo(o2);
      }
    };
  }
}
