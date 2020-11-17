package dev.fiki.forgehax.api.cmd.argument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

@Getter
@AllArgsConstructor
@Builder
public class RawArgument<E> implements IArgument<E> {
  @NonNull
  private final Class<E> type;
  @NonNull
  private final Function<String, E> parser;
  @NonNull
  private final Function<E, String> converter;

  private final Comparator<E> comparator;
  private final String label;
  private final E defaultValue;

  @Builder.Default
  private int minArgumentsConsumed = 1;

  @Builder.Default
  private int maxArgumentsConsumed = 1;

  @Override
  public Class<E> type() {
    return type;
  }

  @Override
  public E parse(String value) {
    return parser.apply(value);
  }

  @Override
  public String convert(E value) {
    return converter.apply(value);
  }

  @Override
  public Comparator<E> comparator() {
    return comparator;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RawArgument<?> argument = (RawArgument<?>) o;
    return label.equalsIgnoreCase(argument.label);
  }

  @Override
  public int hashCode() {
    return Objects.hash(label.toLowerCase());
  }

  public static class RawArgumentBuilder<E> {
    public RawArgumentBuilder<E> optional() {
      return minArgumentsConsumed(0);
    }
  }
}
