package com.matt.forgehax.util.draw;

import java.awt.*;
import uk.co.hexeption.thx.ttf.MinecraftFontRenderer;

/** Created on 9/2/2017 by fr1kin */
public interface Fonts {
  MinecraftFontRenderer ARIAL =
      new MinecraftFontRenderer(new Font("Arial", Font.PLAIN, 18), true, true);
  MinecraftFontRenderer CAMBRIA =
      new MinecraftFontRenderer(new Font("Cambria", Font.PLAIN, 18), true, true);
  MinecraftFontRenderer GEORGIA =
      new MinecraftFontRenderer(new Font("Georgia", Font.PLAIN, 18), true, true);
}
