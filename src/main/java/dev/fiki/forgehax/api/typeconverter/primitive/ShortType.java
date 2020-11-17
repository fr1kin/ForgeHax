package dev.fiki.forgehax.api.typeconverter.primitive;

import dev.fiki.forgehax.api.typeconverter.TypeConverter;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * Created on 3/23/2017 by fr1kin
 */
public class ShortType extends TypeConverter<Short> {
  
  @Override
  public String label() {
    return "short";
  }
  
  @Override
  public Class<Short> type() {
    return Short.class;
  }
  
  @Override
  public Short parse(String value) {
    return Short.parseShort(value);
  }
  
  @Override
  public String convert(Short value) {
    return Short.toString(value);
  }
  
  @Nullable
  @Override
  public Comparator<Short> comparator() {
    return Short::compare;
  }
}
