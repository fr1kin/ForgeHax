package dev.fiki.forgehax.api.typeconverter.type;

import dev.fiki.forgehax.api.color.Color;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.typeconverter.TypeConverter;

public class ColorType extends TypeConverter<Color> {
  @Override
  public String label() {
    return "color";
  }

  @Override
  public Class<Color> type() {
    return Color.class;
  }

  @Override
  public Color parse(String value) {
    Color color = Colors.map().color(value);
    if (color != null) {
      return color;
    } else {
      if(value.startsWith("0x")) {
        return Color.of(Integer.parseUnsignedInt(value.substring(2), 16));
      } else {
        String[] ss = value.split(" ");

        if(ss.length < 3) throw new IllegalArgumentException("color expected 3 or 4 arguments, got " + ss.length);

        return ss.length == 3
            ? Color.of(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]), Integer.parseInt(ss[2]))
            : Color.of(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]), Integer.parseInt(ss[2]), Integer.parseInt(ss[3]));
      }
    }
  }

  @Override
  public String convert(Color value) {
    return value.getName() != null ? value.getName() : value.toString();
  }
}
