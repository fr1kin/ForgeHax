package dev.fiki.forgehax.api.color;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import javax.annotation.Nullable;

public class ColorMap {
  private final Object2IntOpenHashMap<String> NAME_TO_HASH = new Object2IntOpenHashMap<>();
  private final Int2ObjectOpenHashMap<String> HASH_TO_NAME = new Int2ObjectOpenHashMap<>();

  {
    NAME_TO_HASH.defaultReturnValue(Integer.MAX_VALUE);
    HASH_TO_NAME.defaultReturnValue(null);
  }

  ColorMap() {
    register(128, 0, 0, "maroon");
    register(139, 0, 0, "dark_red");
    register(165, 42, 42, "brown");
    register(178, 34, 34, "firebrick");
    register(220, 20, 60, "crimson");
    register(255, 0, 0, "red");
    register(255, 99, 71, "tomato");
    register(255, 127, 80, "coral");
    register(205, 92, 92, "indian_red");
    register(240, 128, 128, "light_coral");
    register(233, 150, 122, "dark_salmon");
    register(250, 128, 114, "salmon");
    register(255, 160, 122, "light_salmon");
    register(255, 69, 0, "orange_red");
    register(255, 140, 0, "dark_orange");
    register(255, 165, 0, "orange");
    register(255, 215, 0, "gold");
    register(184, 134, 11, "dark_golden_rod");
    register(218, 165, 32, "golden_rod");
    register(238, 232, 170, "pale_golden_rod");
    register(189, 183, 107, "dark_khaki");
    register(240, 230, 140, "khaki");
    register(128, 128, 0, "olive");
    register(255, 255, 0, "yellow");
    register(154, 205, 50, "yellow_green");
    register(85, 107, 47, "dark_olive_green");
    register(107, 142, 35, "olive_drab");
    register(124, 252, 0, "lawn_green");
    register(127, 255, 0, "chart_reuse");
    register(173, 255, 47, "green_yellow");
    register(0, 100, 0, "dark_green");
    register(0, 128, 0, "green");
    register(34, 139, 34, "forest_green");
    register(0, 255, 0, "lime");
    register(50, 205, 50, "lime_green");
    register(144, 238, 144, "light_green");
    register(152, 251, 152, "pale_green");
    register(143, 188, 143, "dark_sea_green");
    register(0, 250, 154, "medium_spring_green");
    register(0, 255, 127, "spring_green");
    register(46, 139, 87, "sea_green");
    register(102, 205, 170, "medium_aqua_marine");
    register(60, 179, 113, "medium_sea_green");
    register(32, 178, 170, "light_sea_green");
    register(47, 79, 79, "dark_slate_gray");
    register(0, 128, 128, "teal");
    register(0, 139, 139, "dark_cyan");
    register(0, 255, 255, "aqua");
    register(0, 255, 255, "cyan");
    register(224, 255, 255, "light_cyan");
    register(0, 206, 209, "dark_turquoise");
    register(64, 224, 208, "turquoise");
    register(72, 209, 204, "medium_turquoise");
    register(175, 238, 238, "pale_turquoise");
    register(127, 255, 212, "aqua_marine");
    register(176, 224, 230, "powder_blue");
    register(95, 158, 160, "cadet_blue");
    register(70, 130, 180, "steel_blue");
    register(100, 149, 237, "corn_flower_blue");
    register(0, 191, 255, "deep_sky_blue");
    register(30, 144, 255, "dodger_blue");
    register(173, 216, 230, "light_blue");
    register(135, 206, 235, "sky_blue");
    register(135, 206, 250, "light_sky_blue");
    register(25, 25, 112, "midnight_blue");
    register(0, 0, 128, "navy");
    register(0, 0, 139, "dark_blue");
    register(0, 0, 205, "medium_blue");
    register(0, 0, 255, "blue");
    register(65, 105, 225, "royal_blue");
    register(138, 43, 226, "blue_violet");
    register(75, 0, 130, "indigo");
    register(72, 61, 139, "dark_slate_blue");
    register(106, 90, 205, "slate_blue");
    register(123, 104, 238, "medium_slate_blue");
    register(147, 112, 219, "medium_purple");
    register(139, 0, 139, "dark_magenta");
    register(148, 0, 211, "dark_violet");
    register(153, 50, 204, "dark_orchid");
    register(186, 85, 211, "medium_orchid");
    register(128, 0, 128, "purple");
    register(216, 191, 216, "thistle");
    register(221, 160, 221, "plum");
    register(238, 130, 238, "violet");
    register(255, 0, 255, "magenta", "fuchsia");
    register(218, 112, 214, "orchid");
    register(199, 21, 133, "medium_violet_red");
    register(219, 112, 147, "pale_violet_red");
    register(255, 20, 147, "deep_pink");
    register(255, 105, 180, "hot_pink");
    register(255, 182, 193, "light_pink");
    register(255, 192, 203, "pink");
    register(250, 235, 215, "antique_white");
    register(245, 245, 220, "beige");
    register(255, 228, 196, "bisque");
    register(255, 235, 205, "blanched_almond");
    register(245, 222, 179, "wheat");
    register(255, 248, 220, "corn_silk");
    register(255, 250, 205, "lemon_chiffon");
    register(250, 250, 210, "light_golden_rod_yellow");
    register(255, 255, 224, "light_yellow");
    register(139, 69, 19, "saddle_brown");
    register(160, 82, 45, "sienna");
    register(210, 105, 30, "chocolate");
    register(205, 133, 63, "peru");
    register(244, 164, 96, "sandy_brown");
    register(222, 184, 135, "burly_wood");
    register(210, 180, 140, "tan");
    register(188, 143, 143, "rosy_brown");
    register(255, 228, 181, "moccasin");
    register(255, 222, 173, "navajo_white");
    register(255, 218, 185, "peach_puff");
    register(255, 228, 225, "misty_rose");
    register(255, 240, 245, "lavender_blush");
    register(250, 240, 230, "linen");
    register(253, 245, 230, "old_lace");
    register(255, 239, 213, "papaya_whip");
    register(255, 245, 238, "sea_shell");
    register(245, 255, 250, "mint_cream");
    register(112, 128, 144, "slate_gray");
    register(119, 136, 153, "light_slate_gray");
    register(176, 196, 222, "light_steel_blue");
    register(230, 230, 250, "lavender");
    register(255, 250, 240, "floral_white");
    register(240, 248, 255, "alice_blue");
    register(248, 248, 255, "ghost_white");
    register(240, 255, 240, "honeydew");
    register(255, 255, 240, "ivory");
    register(240, 255, 255, "azure");
    register(255, 250, 250, "snow");
    register(0, 0, 0, "black");
    register(105, 105, 105, "dim_gray", "dim_grey");
    register(128, 128, 128, "gray", "grey");
    register(169, 169, 169, "dark_gray", "dark_grey");
    register(192, 192, 192, "silver");
    register(211, 211, 211, "light_gray", "light_grey");
    register(220, 220, 220, "gainsboro");
    register(245, 245, 245, "white_smoke");
    register(255, 255, 255, "white");
  }

  void register(int r, int g, int b, String... names) {
    int hash = Color.of(r, g, b).toBuffer();
    for (String name : names) {
      HASH_TO_NAME.put(hash, name);
      NAME_TO_HASH.put(name, hash);
    }
  }

  public int getHash(String colorName) {
    return NAME_TO_HASH.getInt(colorName);
  }

  public String getName(int hash) {
    return HASH_TO_NAME.get(hash);
  }

  @Nullable
  public Color color(String colorName) {
    return NAME_TO_HASH.containsKey(colorName) ? Color.of(getHash(colorName)) : null;
  }

  public Color colorNonNull(String colorName) {
    Color ret = color(colorName);

    if (ret == null) {
      throw new Error("Could not find color by name \"" + colorName + "\"");
    }

    return ret;
  }
}
