package dev.fiki.forgehax.main.util.typeconverter;

import java.util.Comparator;

public interface IConverter<E> {
  Class<E> type();

  E parse(String value);
  String convert(E value);

  Comparator<E> comparator();
}
