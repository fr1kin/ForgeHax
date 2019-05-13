package com.matt.forgehax.util;

import com.matt.forgehax.Globals;
import net.minecraft.client.renderer.texture.NativeImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

/** Created by Babbaj on 11/7/2017. */
public class ImageUtils implements Globals {

  public static BufferedImage createResizedCopy(
      Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha) {
    int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
    BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
    Graphics2D g = scaledBI.createGraphics();
    if (preserveAlpha) {
      g.setComposite(AlphaComposite.Src);
    }
    g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
    g.dispose();
    return scaledBI;
  }

  @Deprecated
  public static BufferedImage getImageFromUrl(String link) {
    BufferedImage image = null;
    try {
      URL url = new URL(link);
      image = ImageIO.read(url);
    } catch (Exception e) {
      LOGGER.error("Failed to download Image");
    }
    return image;
  }

  public static int[][] imageToArray(BufferedImage imageIn) {
    int width = imageIn.getWidth();
    int height = imageIn.getHeight();

    int[][] data = new int[height][width]; // array of rows

    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        data[i][j] = imageIn.getRGB(i, j);
      }
    }
    return data;
  }

  public static NativeImage toNativeImage(BufferedImage image) {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(image, "PNG", outputStream); // not sure if PNG is always correct
      byte[] bytes = outputStream.toByteArray();
      ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
      buffer.put(bytes);
      buffer.clear();

      return NativeImage.read(buffer);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

  }
}
