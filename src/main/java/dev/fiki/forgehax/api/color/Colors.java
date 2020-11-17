package dev.fiki.forgehax.api.color;

/**
 * Created on 2/6/2018 by fr1kin
 */
public interface Colors {
  ColorMap MAP = new ColorMap();

  static ColorMap map() {
    return MAP;
  }
  
  Color WHITE = MAP.getNonNull("white");
  Color BLACK = MAP.getNonNull("black");
  Color RED = MAP.getNonNull("red");
  Color GREEN = MAP.getNonNull("green");
  Color BLUE = MAP.getNonNull("blue");
  Color CYAN = MAP.getNonNull("cyan");
  Color ORANGE = MAP.getNonNull("orange");
  Color PURPLE = MAP.getNonNull("purple");
  Color MAGENTA = MAP.getNonNull("magenta");
  Color GRAY = MAP.getNonNull("gray");
  Color DARK_RED = MAP.getNonNull("dark_red");
  Color DARK_GRAY = MAP.getNonNull("dark_gray");
  Color YELLOW = MAP.getNonNull("yellow");
}
