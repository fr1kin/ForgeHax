package com.matt.forgehax.util.typeconverter.types;

import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.typeconverter.TypeConverter;
import java.util.Comparator;
import javax.annotation.Nullable;

/** Created on 3/23/2017 by fr1kin */
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
    return SafeConverter.toInteger(value);
  }

  @Override
  public String toString(Integer value) {
    return Integer.toString(value);
  }

  @Nullable
  @Override
  public Comparator<Integer> comparator() {
    return Integer::compare;
  }
}
