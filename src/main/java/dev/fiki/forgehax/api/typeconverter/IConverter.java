package dev.fiki.forgehax.api.typeconverter;

import java.util.Comparator;

public interface IConverter<E> {
  Class<E> type();

  E parse(String value);
  String convert(E value);

  default String print(E value) {
    return convert(value);
  }

  Comparator<E> comparator();
}
