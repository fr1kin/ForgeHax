package dev.fiki.forgehax.main.util.draw.font;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import static dev.fiki.forgehax.main.Common.getLogger;

/**
 * Stores font information which is used within a font renderer.
 * Created by Brandon on 9/26/2016.
 */
public final class FontData {

  private final CharacterData[] characterBounds = new CharacterData[256];
  private DynamicTexture texture;
  private int fontHeight = 0;

  private int textureWidth, textureHeight;

  /**
   * Creates a font image and the character locations within the font image.
   */
  public FontData setFont(Font font, boolean antialias) {
    return setFont(font, antialias, antialias, 16, 2);
  }

  /**
   * Creates a font image and the character locations within the font image.
   */
  private FontData setFont(Font font, boolean antialias, boolean fractionalmetrics, int characterCount, int padding) {
    // Font metrics can be created from the font without having to create a graphics object.
    FontMetrics fontMetrics = new Canvas().getFontMetrics(font);

    int charHeight = 0, positionX = 0, positionY = 0; //, textureWidth = 0, textureHeight = 0

    // We'll be generating the character bounds as well as an appropriate texture width and height for the font to be rendered onto.
    for (int i = 0; i < characterBounds.length; i++) {
      char character = (char) i;

      int height = fontMetrics.getHeight();
      int width = fontMetrics.charWidth(character);

      if (i != 0 && i % characterCount == 0) {
        positionX = padding;
        positionY += charHeight + padding;
        charHeight = 0;
      }

      if (height > charHeight) {
        charHeight = height;
        if (charHeight > fontHeight)
          fontHeight = charHeight;
      }

      characterBounds[i] = new CharacterData(positionX, positionY, width, height);

      positionX += width + padding;

      // Ensure that our texture can fit the characters.
      if (positionX + width + padding > textureWidth)
        textureWidth = positionX + width + padding;

      if (positionY + height + padding > textureHeight)
        textureHeight = positionY + height + padding;
    }

    // Image we'll use to store our font onto for rendering.
    BufferedImage image = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics2D = (Graphics2D) image.getGraphics();
    graphics2D.setFont(font);
    fontMetrics = graphics2D.getFontMetrics(font);

    // Give blank background
    graphics2D.setColor(new Color(255, 255, 255, 0));
    graphics2D.fillRect(0, 0, textureWidth, textureHeight);

    // Set color to white for rendering the font onto the texture.
    graphics2D.setColor(Color.WHITE);

    // Set render hints
    graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalmetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antialias ? RenderingHints.VALUE_TEXT_ANTIALIAS_GASP : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, antialias ? RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY : RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

    for (int i = 0; i < characterBounds.length; i++) {
      // Draw the char onto the final image we'll be using to render this font.
      graphics2D.drawString(String.valueOf((char) i), characterBounds[i].x, characterBounds[i].y + fontMetrics.getAscent());
    }

    //int[] pixels = new int[image.getWidth() * image.getHeight()];
    //image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

    try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      ImageIO.write(image, "png", os);
      byte[] png = os.toByteArray();
      ByteBuffer bb = ByteBuffer.allocateDirect(png.length);
      bb.put(png).clear();
      NativeImage nm = NativeImage.read(bb.put(png));
      // TODO: 1.15 fix
      texture = new DynamicTexture(Objects.requireNonNull(nm, "Missing font texture"));
    } catch (IOException e) {
      getLogger().error(e, e);
    }

    return this;
  }

  /**
   * Binds the font texture.
   */
  public void bind() {
    texture.bindTexture();
  }

  /**
   * @return The bounds of the character within the font image.
   */
  public CharacterData getCharacterBounds(char character) {
    return characterBounds[character];
  }

  /**
   * @return The total width of each character within the string.
   */
  public int getStringWidth(String text) {
    int width = 0;
    for (char c : text.toCharArray()) {
      width += characterBounds[c].width;
    }
    return width;
  }

  /**
   * @return True if the character has been mapped in this font.
   */
  public boolean hasBounds(char character) {
    return character >= 0 && character < 256;
  }

  /**
   * @return True if the font has not been set.
   */
  public boolean hasFont() {
    return texture != null;
  }

  public int getFontHeight() {
    return fontHeight;
  }

  public int getTextureWidth() {
    return textureWidth;
  }

  public int getTextureHeight() {
    return textureHeight;
  }

  /**
   * Character information regarding it's position within the font texture and the character's width/height within the font.
   */
  public class CharacterData {

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public CharacterData(int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
    }
  }

}
