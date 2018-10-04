package com.matt.forgehax.util.typeconverter.types;

import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.typeconverter.TypeConverter;
import java.util.Comparator;
import javax.annotation.Nullable;

/** Created on 3/23/2017 by fr1kin */
public class LongType extends TypeConverter<Long> {
  @Override
  public String label() {
    return "long";
  }

  @Override
  public Class<Long> type() {
    return Long.class;
  }

  @Override
  public Long parse(String value) {
    return SafeConverter.toLong(value);
  }

  @Override
  public String toString(Long value) {
    return Long.toString(value);
  }

  @Nullable
  @Override
  public Comparator<Long> comparator() {
    return Long::compare;
  }
}
