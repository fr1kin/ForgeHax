package dev.fiki.forgehax.api.typeconverter;

import java.util.Comparator;
import java.util.Objects;

/**
 * Created on 3/23/2017 by fr1kin
 */
public abstract class TypeConverter<E> implements IConverter<E> {
  public abstract String label();
  
  public abstract Class<E> type();
  
  public abstract E parse(String value);
  
  public abstract String convert(E value);
  
  public Comparator<E> comparator() {
    return ((o1, o2) -> 0);
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof IConverter && ((IConverter) obj).type().equals(this.type()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(type());
  }
}
