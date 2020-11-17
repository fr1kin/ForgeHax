package dev.fiki.forgehax.api.cmd.argument;

import dev.fiki.forgehax.api.typeconverter.IConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Comparator;

@Getter
@AllArgsConstructor
@Builder
public class ConverterArgument<E> implements IArgument<E> {
  private final IConverter<E> converter;
  private final String label;
  private final E defaultValue;
  private final int minArgumentsConsumed;
  private final int maxArgumentsConsumed;

  @Override
  public Class<E> type() {
    return converter.type();
  }

  @Override
  public E parse(String value) {
    return converter.parse(value);
  }

  @Override
  public String convert(E value) {
    return converter.convert(value);
  }

  @Override
  public Comparator<E> comparator() {
    return converter.comparator();
  }

  public static class ConverterArgumentBuilder<E> {
    public ConverterArgumentBuilder<E> optional() {
      return minArgumentsConsumed(0);
    }
  }
}
