package dev.fiki.forgehax.api.draw.font;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.api.color.Color;
import dev.fiki.forgehax.api.draw.SurfaceHelper;

/**
 * Basic implementation of the FontRenderer interface.
 */
public class BasicFontRenderer implements FontRenderer {

  protected int kerning = 0;

  protected final FontData fontData = new FontData();

  public BasicFontRenderer() {
  }

  @Override
  public int drawString(FontData fontData, String text, int x, int y, int color) {
    if (!fontData.hasFont())
      return 0;
    RenderSystem.enableBlend();
    fontData.bind();
    Color.of(color).glSetColor4f();
    int size = text.length();
    for (int i = 0; i < size; i++) {
      char character = text.charAt(i);
      if (fontData.hasBounds(character)) {
        FontData.CharacterData area = fontData.getCharacterBounds(character);
        SurfaceHelper.drawTextureRect(x, y, area.width, area.height,
            (float) area.x / fontData.getTextureWidth(),
            (float) area.y / fontData.getTextureHeight(),
            (float) (area.x + area.width) / fontData.getTextureWidth(),
            (float) (area.y + area.height) / fontData.getTextureHeight());
        x += area.width + kerning;
      }
    }
    return x;
  }

  @Override
  public int drawString(String text, int x, int y, int color) {
    return drawString(fontData, text, x, y, color);
  }

  public int getKerning() {
    return kerning;
  }

  public void setKerning(int kerning) {
    this.kerning = kerning;
  }

  @Override
  public FontData getFontData() {
    return fontData;
  }
}
