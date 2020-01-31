package dev.fiki.forgehax.main.util.typeconverter.types;

import dev.fiki.forgehax.main.util.typeconverter.TypeConverter;
import dev.fiki.forgehax.main.util.SafeConverter;

import java.util.Comparator;
import javax.annotation.Nullable;

/**
 * Created on 3/23/2017 by fr1kin
 */
public class FloatType extends TypeConverter<Float> {
  
  @Override
  public String label() {
    return "float";
  }
  
  @Override
  public Class<Float> type() {
    return Float.class;
  }
  
  @Override
  public Float parse(String value) {
    return SafeConverter.toFloat(value);
  }
  
  @Override
  public String toString(Float value) {
    return Float.toString(value);
  }
  
  @Nullable
  @Override
  public Comparator<Float> comparator() {
    return Float::compare;
  }
}
