package dev.fiki.forgehax.api.color;

/**
 * Created on 2/5/2018 by fr1kin
 */
public class ColorBuffer extends Color {
  
  private static final Color FACTORY = new ColorBuffer();
  
  public static Color getFactory() {
    return FACTORY;
  }
  
  //
  //
  //
  
  private final int buffer;
  
  private ColorBuffer() {
    this(0);
  }
  
  private ColorBuffer(int buffer) {
    this.buffer = buffer;
  }
  
  @Override
  public Color set(int buffer) {
    return new ColorBuffer(buffer);
  }
  
  @Override
  public Color set(float red, float green, float blue, float alpha) {
    return set(
        (int) (red * 255.f), (int) (green * 255.f), (int) (blue * 255.f), (int) (alpha * 255.f));
  }
  
  @Override
  public int getRed() {
    return toBuffer() >> 16 & 255;
  }
  
  @Override
  public int getGreen() {
    return toBuffer() >> 8 & 255;
  }
  
  @Override
  public int getBlue() {
    return toBuffer() & 255;
  }
  
  @Override
  public int getAlpha() {
    return toBuffer() >> 24 & 255;
  }
  
  @Override
  public float getRedAsFloat() {
    return (float) getRed() / 255.f;
  }
  
  @Override
  public float getGreenAsFloat() {
    return (float) getGreen() / 255.f;
  }
  
  @Override
  public float getBlueAsFloat() {
    return (float) getBlue() / 255.f;
  }
  
  @Override
  public float getAlphaAsFloat() {
    return (float) getAlpha() / 255.f;
  }
  
  @Override
  public int toBuffer() {
    return buffer;
  }
  
  @Override
  public float[] toFloatArray() {
    return new float[]{getRedAsFloat(), getGreenAsFloat(), getBlueAsFloat(), getAlphaAsFloat()};
  }

  @Override
  public boolean isBufferType() {
    return true;
  }
}
