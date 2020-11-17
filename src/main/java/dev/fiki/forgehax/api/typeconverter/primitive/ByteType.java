package dev.fiki.forgehax.api.typeconverter.primitive;

import dev.fiki.forgehax.api.typeconverter.TypeConverter;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * Created on 3/23/2017 by fr1kin
 */
public class ByteType extends TypeConverter<Byte> {
  
  @Override
  public String label() {
    return "byte";
  }
  
  @Override
  public Class<Byte> type() {
    return Byte.class;
  }
  
  @Override
  public Byte parse(String value) {
    return Byte.parseByte(value);
  }
  
  @Override
  public String convert(Byte value) {
    return Byte.toString(value);
  }
  
  @Nullable
  @Override
  public Comparator<Byte> comparator() {
    return Byte::compare;
  }
}
