package com.matt.forgehax.util.typeconverter.types;

import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.typeconverter.TypeConverter;
import java.util.Comparator;
import javax.annotation.Nullable;

/** Created on 3/23/2017 by fr1kin */
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
    return SafeConverter.toShort(value);
  }

  @Override
  public String toString(Short value) {
    return Short.toString(value);
  }

  @Nullable
  @Override
  public Comparator<Short> comparator() {
    return Short::compare;
  }
}
