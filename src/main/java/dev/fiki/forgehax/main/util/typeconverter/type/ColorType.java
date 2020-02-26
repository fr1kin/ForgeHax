package dev.fiki.forgehax.main.util.typeconverter.type;

import dev.fiki.forgehax.main.util.color.Color;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.typeconverter.TypeConverter;

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
    switch (value.toLowerCase()) {
      case "white":
        return Colors.WHITE;
      case "black":
        return Colors.BLACK;
      case "red":
        return Colors.RED;
      case "green":
        return Colors.GREEN;
      case "blue":
        return Colors.BLUE;
      case "cyan":
        return Colors.CYAN;
      case "orange":
        return Colors.ORANGE;
      case "purple":
        return Colors.PURPLE;
      case "magenta":
        return Colors.MAGENTA;
      case "gray":
      case "grey":
        return Colors.GRAY;
      case "yellow":
        return Colors.YELLOW;
      case "dark red":
        return Colors.DARK_RED;
      default:
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
    return String.valueOf(value.toString());
  }
}
