package dev.fiki.forgehax.main.util.draw.font;

import java.awt.*;

/**
 * Created on 9/2/2017 by fr1kin
 */
public interface Fonts {
  MinecraftFontRenderer ARIAL = getArial();

  static MinecraftFontRenderer getArial() {
    MinecraftFontRenderer arial = new MinecraftFontRenderer();
    arial.setFont(new Font("Arial", Font.PLAIN, 18), true);
    return arial;
  }
}
