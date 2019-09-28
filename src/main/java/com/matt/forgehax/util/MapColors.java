package com.matt.forgehax.util;

import com.matt.forgehax.util.color.Color;
import net.minecraft.block.material.MapColor;

/**
 * Created by Babbaj on 8/19/2017.
 */
public class MapColors {
  
  /**
   * list of all possible colors used on maps, 4 for each base color
   */
  private static final int[] COLOR_LIST;
  /**
   * list of base colors from {@link net.minecraft.block.material.MapColor}
   */
  private static final int[] BASE_COLORS;
  
  static {
    // find the length of array that contains non null map colors
    int baseColorsLength = 0;
    for (int i = MapColor.COLORS.length - 1; i >= 0; i--) {
      if (MapColor.COLORS[i] != null) {
        baseColorsLength = i + 1;
        break;
      }
    }
    BASE_COLORS = new int[baseColorsLength];
    COLOR_LIST = new int[baseColorsLength * 4];
    
    for (int i = 0; i < BASE_COLORS.length; i++) {
      // get integer color values from MapColor object list
      BASE_COLORS[i] = MapColor.COLORS[i].colorValue;
    }
    
    for (int i = 0;
        i < BASE_COLORS.length;
        i++) { // generates full list of colors from the list of base colors
      int[] rgb = Color.of(BASE_COLORS[i]).toIntegerArray();
      COLOR_LIST[i * 4] = Color.of(
          (rgb[0] * 180) / 255,
          (rgb[1] * 180) / 255,
          (rgb[2] * 180) / 255,
          0
      ).toBuffer();
      COLOR_LIST[i * 4 + 1] = Color.of(
          (rgb[0] * 220) / 255,
          (rgb[1] * 220) / 255,
          (rgb[2] * 220) / 255,
          0
      ).toBuffer();
      COLOR_LIST[i * 4 + 2] = BASE_COLORS[i];
      COLOR_LIST[i * 4 + 3] = Color.of(
          (rgb[0] * 135) / 255,
          (rgb[1] * 135) / 255,
          (rgb[2] * 135) / 255,
          0
      ).toBuffer();
    }
  }
  
  public static int getColor(int index) {
    return COLOR_LIST[index];
  }
  
  public static int colorListLength() {
    return COLOR_LIST.length;
  }
  
  public static int getBaseColor(int index) {
    return BASE_COLORS[index];
  }
  
  public static int baseColorListLength() {
    return BASE_COLORS.length;
  }
}
