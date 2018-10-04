package com.matt.forgehax.util.filter;

import com.matt.forgehax.util.serialization.ISerializableJson;

/** Created on 8/23/2017 by fr1kin */
public abstract class FilterElement implements ISerializableJson {
  public abstract String name();

  public abstract boolean matches(Object o);

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract String toString();
}
