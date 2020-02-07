package dev.fiki.forgehax.main.util.typeconverter.types;

import dev.fiki.forgehax.main.util.typeconverter.TypeConverter;
import java.util.Comparator;
import javax.annotation.Nullable;

/**
 * Created on 3/23/2017 by fr1kin
 */
public class DoubleType extends TypeConverter<Double> {
  
  @Override
  public String label() {
    return "double";
  }
  
  @Override
  public Class<Double> type() {
    return Double.class;
  }
  
  @Override
  public Double parse(String value) {
    return Double.parseDouble(value);
  }
  
  @Override
  public String convert(Double value) {
    return Double.toString(value);
  }
  
  @Nullable
  @Override
  public Comparator<Double> comparator() {
    return Double::compare;
  }
}
