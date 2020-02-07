package dev.fiki.forgehax.main.util.typeconverter.types;

import dev.fiki.forgehax.main.util.typeconverter.TypeConverter;
import java.util.Comparator;
import javax.annotation.Nullable;

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
