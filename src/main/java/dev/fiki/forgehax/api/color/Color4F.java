package dev.fiki.forgehax.api.color;

import java.util.Arrays;

/**
 * Created on 2/6/2018 by fr1kin
 */
public class Color4F extends Color {
  
  private static final Color FACTORY = new Color4F();
  
  public static Color getFactory() {
    return FACTORY;
  }
  
  //
  //
  //
  
  private final float[] color = new float[4];
  
  private Color4F() {
  }
  
  private Color4F(float red, float green, float blue, float alpha) {
    color[0] = red;
    color[1] = green;
    color[2] = blue;
    color[3] = alpha;
  }
  
  @Override
  public Color set(int buffer) {
    return set(
        (float) (buffer >> 16 & 255) / 255.f,
        (float) (buffer >> 8 & 255) / 255.f,
        (float) (buffer & 255) / 255.f,
        (float) (buffer >> 24 & 255) / 255.f);
  }
  
  @Override
  public Color set(float red, float green, float blue, float alpha) {
    return new Color4F(red, green, blue, alpha);
  }
  
  @Override
  public int getRed() {
    return (int) (getRedAsFloat() * 255);
  }
  
  @Override
  public int getGreen() {
    return (int) (getRedAsFloat() * 255);
  }
  
  @Override
  public int getBlue() {
    return (int) (getRedAsFloat() * 255);
  }
  
  @Override
  public int getAlpha() {
    return (int) (getRedAsFloat() * 255);
  }
  
  @Override
  public float getRedAsFloat() {
    return color[0];
  }
  
  @Override
  public float getGreenAsFloat() {
    return color[1];
  }
  
  @Override
  public float getBlueAsFloat() {
    return color[2];
  }
  
  @Override
  public float getAlphaAsFloat() {
    return color[3];
  }
  
  @Override
  public float[] toFloatArray() {
    return Arrays.copyOf(color, color.length);
  }

  @Override
  public boolean isBufferType() {
    return false;
  }
}
