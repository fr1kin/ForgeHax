package dev.fiki.forgehax.api.color;

/**
 * Created on 2/6/2018 by fr1kin
 */
public interface Colors {
  ColorMap MAP = new ColorMap();

  static ColorMap map() {
    return MAP;
  }
  
  Color WHITE = MAP.colorNonNull("white");
  Color BLACK = MAP.colorNonNull("black");
  Color RED = MAP.colorNonNull("red");
  Color GREEN = MAP.colorNonNull("green");
  Color BLUE = MAP.colorNonNull("blue");
  Color CYAN = MAP.colorNonNull("cyan");
  Color ORANGE = MAP.colorNonNull("orange");
  Color PURPLE = MAP.colorNonNull("purple");
  Color MAGENTA = MAP.colorNonNull("magenta");
  Color GRAY = MAP.colorNonNull("gray");
  Color DARK_RED = MAP.colorNonNull("dark_red");
  Color DARK_GRAY = MAP.colorNonNull("dark_gray");
  Color YELLOW = MAP.colorNonNull("yellow");
}
