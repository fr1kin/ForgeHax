package dev.fiki.forgehax.api.typeconverter.type;

import dev.fiki.forgehax.api.typeconverter.TypeConverter;
import lombok.AllArgsConstructor;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;

@AllArgsConstructor
public class EnumType<E extends Enum<E>> extends TypeConverter<E> {
  private final Class<E> type;

  @Override
  public String label() {
    return type.getSimpleName();
  }

  @Override
  public Class<E> type() {
    return type;
  }

  @Override
  public E parse(String value) {
    final String lowerValue = value.toLowerCase();
    return Arrays.stream(type().getEnumConstants())
        .filter(en -> en.name().toLowerCase().startsWith(lowerValue))
        .min(Comparator.comparingInt(Enum::ordinal))
        .orElseGet(() -> {
          E[] values = type().getEnumConstants();
          try {
            int index = Integer.parseInt(value);
            return values[MathHelper.clamp(index, 0, values.length - 1)];
          } catch (NumberFormatException e) {
            return values[0];
          }
        });
  }

  @Override
  public String convert(E value) {
    return value.name();
  }

  @Nullable
  @Override
  public Comparator<E> comparator() {
    return Enum::compareTo;
  }
}
