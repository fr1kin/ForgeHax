package dev.fiki.forgehax.api.draw.font;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.api.draw.SurfaceHelper;

import java.awt.*;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

/**
 * Implementation of the basic font renderer for minecraft.
 *
 * @author Halalaboos
 * @since Nov 23, 2013
 */
public final class MinecraftFontRenderer extends BasicFontRenderer {

  private final FontData boldFont = new FontData();

  private final FontData italicFont = new FontData();

  private final FontData boldItalicFont = new FontData();

  private final int[] colorCode = new int[32];

  private final String colorcodeIdentifiers = "0123456789abcdefklmnor";

  public MinecraftFontRenderer() {
    for (int index = 0; index < 32; ++index) {
      int noClue = (index >> 3 & 1) * 85;
      int red = (index >> 2 & 1) * 170 + noClue;
      int green = (index >> 1 & 1) * 170 + noClue;
      int blue = (index & 1) * 170 + noClue;

      if (index == 6) {
        red += 85;
      }

      if (index >= 16) {
        red /= 4;
        green /= 4;
        blue /= 4;
      }

      this.colorCode[index] = (red & 255) << 16 | (green & 255) << 8 | blue & 255;
    }
  }

  @Override
  public int drawString(String text, int x, int y, int color) {
    return drawString(text, x, y, color, false);
  }

  public int drawStringWithShadow(String text, int x, int y, int color) {
    return Math.max(drawString(text, x + 1, y + 1, color, true), drawString(text, x, y, color, false));
  }

  /**
   * Renders text starting with the middle of the string at the given x, y positions. Includes a shadow effect as well.
   */
  public void drawCenteredStringWithShadow(String text, int x, int y, int color) {
    drawStringWithShadow(text, x - getStringWidth(text) / 2, y - getStringHeight(text) / 2, color);
  }

  /**
   * Renders text starting with the middle of the string at the given x, y positions.
   */
  public void drawCenteredString(String text, int x, int y, int color) {
    drawString(text, x - getStringWidth(text) / 2, y - getStringHeight(text) / 2, color);
  }

  /**
   * Renders text using the color code rules within the default Minecraft font renderer.
   */
  public int drawString(String text, int x, int y, int color, boolean shadow) {
    if (text == null)
      return 0;
    if (color == 553648127)
      color = 0xFFFFFF;

    if ((color & -67108864) == 0) {
      color |= -16777216;
    }

    // Shadow effect
    if (shadow) {
      color = (color & 16579836) >> 2 | color & -16777216;
    }

    // Current rendering information.
    FontData currentFont = fontData;
    float alpha = (color >> 24 & 0xff) / 255F;
    boolean randomCase = false, bold = false,
        italic = false, strikethrough = false,
        underline = false;

    // Multiplied positions since we'll be rendering this at half scale (to look nice!)
    x *= 2F;
    y *= 2F;
    RenderSystem.pushMatrix();
    RenderSystem.scalef(0.5F, 0.5F, 0.5F);
    RenderSystem.enableAlphaTest();
    RenderSystem.enableBlend();
    RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    RenderSystem.color4f((float) (color >> 16 & 255) / 255.0F, (float) (color >> 8 & 255) / 255.0F, (float) (color & 255) / 255.0F, alpha);
    int size = text.length();
    currentFont.bind();
    for (int i = 0; i < size; i++) {
      char character = text.charAt(i);
      if (character == '\247' && i < size && i + 1 < size) {
        int colorIndex = colorcodeIdentifiers.indexOf(text.charAt(i + 1));
        if (colorIndex < 16) { // coloring
          bold = false;
          italic = false;
          randomCase = false;
          underline = false;
          strikethrough = false;
          currentFont = fontData;
          currentFont.bind();

          if (colorIndex < 0 || colorIndex > 15) {
            colorIndex = 15;
          }

          if (shadow) {
            colorIndex += 16;
          }

          int colorcode = colorCode[colorIndex];
          RenderSystem.color4f((float) (colorcode >> 16 & 255) / 255.0F, (float) (colorcode >> 8 & 255) / 255.0F, (float) (colorcode & 255) / 255.0F, alpha);
        } else if (colorIndex == 16) { // random case
          randomCase = true;
        } else if (colorIndex == 17) { // bold
          bold = true;
          if (italic) {
            currentFont = boldItalicFont;
            currentFont.bind();
          } else {
            currentFont = boldFont;
            currentFont.bind();
          }
        } else if (colorIndex == 18) { // strikethrough
          strikethrough = true;
        } else if (colorIndex == 19) { // underline
          underline = true;
        } else if (colorIndex == 20) { // italic
          italic = true;
          if (bold) {
            currentFont = boldItalicFont;
            currentFont.bind();
          } else {
            currentFont = italicFont;
            currentFont.bind();
          }
        } else if (colorIndex == 21) { // reset
          bold = false;
          italic = false;
          randomCase = false;
          underline = false;
          strikethrough = false;
          RenderSystem.color4f((float) (color >> 16 & 255) / 255.0F, (float) (color >> 8 & 255) / 255.0F, (float) (color & 255) / 255.0F, alpha);
          currentFont = fontData;
          currentFont.bind();
        }
        i++;
      } else {
        if (currentFont.hasBounds(character)) {
          if (randomCase) {
            char newChar = 0;
            while (currentFont.getCharacterBounds(newChar).width != currentFont.getCharacterBounds(character).width)
              newChar = (char) (Math.random() * 256);
            character = newChar;
          }
          FontData.CharacterData area = currentFont.getCharacterBounds(character);
          SurfaceHelper.drawTextureRect(x, y, area.width, area.height,
              (float) area.x / currentFont.getTextureWidth(),
              (float) area.y / currentFont.getTextureHeight(),
              (float) (area.x + area.width) / currentFont.getTextureWidth(),
              (float) (area.y + area.height) / currentFont.getTextureHeight());
          if (strikethrough)
            SurfaceHelper.drawLine(x, y + area.height / 4 + 2, x + area.width / 2, y + area.height / 4 + 2, 1F);
          if (underline)
            SurfaceHelper.drawLine(x, y + area.height / 2, x + area.width / 2, y + area.height / 2, 1F);
          x += area.width + kerning;
        }
      }
    }
    RenderSystem.popMatrix();
    return x;
  }

  /**
   * @return The height of the text which will be outputted by this font renderer. <br/>
   * This information can normally be acquired through the {@link FontData} object, but with the MinecraftFontRenderer, multiple {@link FontData}s may be used.
   */
  public int getStringHeight(String text) {
    if (text == null)
      return 0;
    int height = 0;
    FontData currentFont = fontData;
    boolean bold = false, italic = false;
    int size = text.length();

    for (int i = 0; i < size; i++) {
      char character = text.charAt(i);
      if (character == '\247' && i < size) {
        int colorIndex = colorcodeIdentifiers.indexOf(character);
        if (colorIndex < 16) { // coloring
          bold = false;
          italic = false;
        } else if (colorIndex == 17) { // bold
          bold = true;
          if (italic)
            currentFont = boldItalicFont;
          else
            currentFont = boldFont;
        } else if (colorIndex == 20) { // italic
          italic = true;
          if (bold)
            currentFont = boldItalicFont;
          else
            currentFont = italicFont;
        } else if (colorIndex == 21) { // reset
          bold = false;
          italic = false;
          currentFont = fontData;
        }
        i++;
      } else {
        if (currentFont.hasBounds(character)) {
          if (currentFont.getCharacterBounds(character).height > height)
            height = currentFont.getCharacterBounds(character).height;
        }
      }
    }
    return height / 2;
  }

  /**
   * @return The width of the text which will be outputted by this font renderer. <br/>
   * This information can normally be acquired through the {@link FontData} object, but with the MinecraftFontRenderer, multiple {@link FontData}s may be used.
   */
  public int getStringWidth(String text) {
    if (text == null)
      return 0;
    int width = 0;
    FontData currentFont = fontData;
    boolean bold = false, italic = false;
    int size = text.length();

    for (int i = 0; i < size; i++) {
      char character = text.charAt(i);
      if (character == '\247' && i < size) {
        int colorIndex = colorcodeIdentifiers.indexOf(character);
        if (colorIndex < 16) { // coloring
          bold = false;
          italic = false;
        } else if (colorIndex == 17) { // bold
          bold = true;
          if (italic)
            currentFont = boldItalicFont;
          else
            currentFont = boldFont;
        } else if (colorIndex == 20) { // italic
          italic = true;
          if (bold)
            currentFont = boldItalicFont;
          else
            currentFont = italicFont;
        } else if (colorIndex == 21) { // reset
          bold = false;
          italic = false;
          currentFont = fontData;
        }
        i++;
      } else {
        if (currentFont.hasBounds(character)) {
          width += currentFont.getCharacterBounds(character).width + kerning;
        }
      }
    }
    return width / 2;
  }

  /**
   * Applies a new font to the default font data as well as the bold, italic, and the bolditalic font data.
   */
  public void setFont(Font font, boolean antialias) {
    this.fontData.setFont(font, antialias);
    this.boldFont.setFont(font.deriveFont(Font.BOLD), antialias);
    this.italicFont.setFont(font.deriveFont(Font.ITALIC), antialias);
    this.boldItalicFont.setFont(font.deriveFont(Font.BOLD | Font.ITALIC), antialias);
  }
}
