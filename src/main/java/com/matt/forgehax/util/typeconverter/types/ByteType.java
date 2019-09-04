package com.matt.forgehax.util.typeconverter.types;

import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.typeconverter.TypeConverter;
import java.util.Comparator;
import javax.annotation.Nullable;

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
    return SafeConverter.toByte(value);
  }

  @Override
  public String toString(Byte value) {
    return Byte.toString(value);
  }

  @Nullable
  @Override
  public Comparator<Byte> comparator() {
    return Byte::compare;
  }
}
