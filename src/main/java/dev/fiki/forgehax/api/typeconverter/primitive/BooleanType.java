package dev.fiki.forgehax.api.typeconverter.primitive;

import dev.fiki.forgehax.api.typeconverter.TypeConverter;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * Created on 3/23/2017 by fr1kin
 */
public class BooleanType extends TypeConverter<Boolean> {
  
  @Override
  public String label() {
    return "bool";
  }
  
  @Override
  public Class<Boolean> type() {
    return Boolean.class;
  }
  
  @Override
  public Boolean parse(String value) {
    return value.toLowerCase().matches("true|tru|tr|t|enabled|enable|enabl|enab|ena|en|e|on|1");
  }
  
  @Override
  public String convert(Boolean value) {
    return Boolean.toString(value);
  }
  
  @Nullable
  @Override
  public Comparator<Boolean> comparator() {
    return Boolean::compare;
  }
}
