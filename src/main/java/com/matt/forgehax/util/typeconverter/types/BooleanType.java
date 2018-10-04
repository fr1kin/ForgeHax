package com.matt.forgehax.util.typeconverter.types;

import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.typeconverter.TypeConverter;
import java.util.Comparator;
import javax.annotation.Nullable;

/** Created on 3/23/2017 by fr1kin */
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
    return SafeConverter.toBoolean(value);
  }

  @Override
  public String toString(Boolean value) {
    return Boolean.toString(value);
  }

  @Nullable
  @Override
  public Comparator<Boolean> comparator() {
    return Boolean::compare;
  }
}
