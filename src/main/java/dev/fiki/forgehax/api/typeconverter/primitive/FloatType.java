package dev.fiki.forgehax.api.typeconverter.primitive;

import dev.fiki.forgehax.api.typeconverter.TypeConverter;

import javax.annotation.Nullable;
import java.util.Comparator;

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
    return Float.parseFloat(value);
  }
  
  @Override
  public String convert(Float value) {
    return Float.toString(value);
  }
  
  @Nullable
  @Override
  public Comparator<Float> comparator() {
    return Float::compare;
  }
}
