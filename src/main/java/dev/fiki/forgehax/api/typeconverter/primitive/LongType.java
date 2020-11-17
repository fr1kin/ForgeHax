package dev.fiki.forgehax.api.typeconverter.primitive;

import dev.fiki.forgehax.api.typeconverter.TypeConverter;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * Created on 3/23/2017 by fr1kin
 */
public class LongType extends TypeConverter<Long> {
  
  @Override
  public String label() {
    return "long";
  }
  
  @Override
  public Class<Long> type() {
    return Long.class;
  }
  
  @Override
  public Long parse(String value) {
    return Long.parseLong(value);
  }
  
  @Override
  public String convert(Long value) {
    return Long.toString(value);
  }
  
  @Nullable
  @Override
  public Comparator<Long> comparator() {
    return Long::compare;
  }
}
