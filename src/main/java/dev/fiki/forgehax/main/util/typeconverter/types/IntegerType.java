package dev.fiki.forgehax.main.util.typeconverter.types;

import dev.fiki.forgehax.main.util.typeconverter.TypeConverter;

import java.util.Comparator;
import javax.annotation.Nullable;

/**
 * Created on 3/23/2017 by fr1kin
 */
public class IntegerType extends TypeConverter<Integer> {
  
  @Override
  public String label() {
    return "int";
  }
  
  @Override
  public Class<Integer> type() {
    return Integer.class;
  }
  
  @Override
  public Integer parse(String value) {
    return Integer.parseInt(value);
  }
  
  @Override
  public String convert(Integer value) {
    return Integer.toString(value);
  }
  
  @Nullable
  @Override
  public Comparator<Integer> comparator() {
    return Integer::compare;
  }
}
