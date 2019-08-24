package com.matt.forgehax.util;

import com.matt.forgehax.Globals;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * Created by Babbaj on 11/7/2017.
 */
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
}
