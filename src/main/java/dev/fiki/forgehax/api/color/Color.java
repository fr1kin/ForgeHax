package dev.fiki.forgehax.api.color;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Objects;

/**
 * Created on 2/5/2018 by fr1kin
 */
public abstract class Color {
  
  public static int clamp(int c) {
    return Math.min(255, Math.max(0, c));
  }
  
  public static float clamp(float c) {
    return Math.min(1.f, Math.max(0.f, c));
  }
  
  public static Color ofInteger() {
    return ColorBuffer.getFactory();
  }
  
  public static Color ofFloat() {
    return Color4F.getFactory();
  }
  
  public static Color of(int buffer) {
    return ofInteger().set(buffer);
  }
  
  public static Color of(int red, int green, int blue, int alpha) {
    return ofInteger().set(red, green, blue, alpha);
  }
  
  public static Color of(int red, int green, int blue) {
    return ofInteger().set(red, green, blue);
  }
  
  public static Color of(int[] color) {
    return ofInteger().set(color);
  }
  
  public static Color of(float red, float green, float blue, float alpha) {
    return ofFloat().set(red, green, blue, alpha);
  }
  
  public static Color of(float red, float green, float blue) {
    return ofFloat().set(red, green, blue);
  }
  
  public static Color of(float[] color) {
    return ofFloat().set(color);
  }
  
  public static Color of(double red, double green, double blue, double alpha) {
    return ofFloat().set(red, green, blue, alpha);
  }
  
  public static Color of(double red, double green, double blue) {
    return ofFloat().set(red, green, blue);
  }
  
  public static Color of(double[] color) {
    return ofFloat().set(color);
  }
  
  //
  //
  //

  public final String getName() {
    return Colors.map().getName(toBuffer());
  }

  public final Color setName(String... colorNames) {
    Colors.map().register(getRed(), getGreen(), getBlue(), colorNames);
    return this;
  }
  
  /**
   * Set color by buffer
   *
   * @param buffer color buffer
   */
  public abstract Color set(int buffer);
  
  /**
   * Set the RGBA accordingly
   *
   * @param red as integer 0-255
   * @param green as integer 0-255
   * @param blue as integer 0-255
   * @param alpha as integer 0-255
   */
  public Color set(int red, int green, int blue, int alpha) {
    return set((red << 16) + (green << 8) + blue + (alpha << 24));
  }
  
  /**
   * Set the RGB accordingly, using 255 (maximum) for the alpha
   *
   * @param red as integer 0-255
   * @param green as integer 0-255
   * @param blue as integer 0-255
   */
  public Color set(int red, int green, int blue) {
    return set(red, green, blue, 255);
  }
  
  public Color set(int[] color) {
    Objects.requireNonNull(color);
    switch (color.length) {
      case 3:
        return set(color[0], color[1], color[2]);
      case 4:
        return set(color[0], color[1], color[2], color[3]);
      default:
        throw new IllegalArgumentException("color[] must be of length 3 or 4");
    }
  }
  
  /**
   * Set the RGBA accordingly
   *
   * @param red as float 0-1
   * @param green as float 0-1
   * @param blue as float 0-1
   * @param alpha as float 0-1
   */
  public abstract Color set(float red, float green, float blue, float alpha);
  
  /**
   * Set the RGBA accordingly, using 1.f (maximum) for the alpha
   *
   * @param red as float 0-1
   * @param green as float 0-1
   * @param blue as float 0-1
   */
  public Color set(float red, float green, float blue) {
    return set(red, green, blue, 1.f);
  }
  
  public Color set(float[] color) {
    Objects.requireNonNull(color);
    switch (color.length) {
      case 3:
        return set(color[0], color[1], color[2]);
      case 4:
        return set(color[0], color[1], color[2], color[3]);
      default:
        throw new IllegalArgumentException("color[] must be of length 3 or 4");
    }
  }
  
  /**
   * Set the RGBA accordingly Will cast all arguments to floats and use set(ffff)
   *
   * @param red as double 0-1
   * @param green as double 0-1
   * @param blue as double 0-1
   * @param alpha as double 0-1
   */
  public Color set(double red, double green, double blue, double alpha) {
    return set((float) red, (float) green, (float) blue, (float) alpha);
  }
  
  /**
   * Set the RGBA accordingly, using 1.D (maximum) for the alpha Will cast all arguments to floats
   * and use set(ffff)
   *
   * @param red as double 0-1
   * @param green as double 0-1
   * @param blue as double 0-1
   */
  public Color set(double red, double green, double blue) {
    return set(red, green, blue, 1.D);
  }
  
  public Color set(double[] color) {
    Objects.requireNonNull(color);
    switch (color.length) {
      case 3:
        return set(color[0], color[1], color[2]);
      case 4:
        return set(color[0], color[1], color[2], color[3]);
      default:
        throw new IllegalArgumentException("color[] must be of length 3 or 4");
    }
  }
  
  /**
   * Red color ranging from 0-255
   *
   * @return red as integer
   */
  public abstract int getRed();
  
  /**
   * Green color ranging from 0-255
   *
   * @return green as integer
   */
  public abstract int getGreen();
  
  /**
   * Blue color ranging from 0-255
   *
   * @return blue as integer
   */
  public abstract int getBlue();
  
  /**
   * Alpha color ranging from 0-255
   *
   * @return alpha as integer
   */
  public abstract int getAlpha();
  
  /**
   * Set red ranging from 0-255
   *
   * @param red as integer
   */
  public Color setRed(int red) {
    return set(red, getGreen(), getBlue(), getAlpha());
  }
  
  /**
   * Set green ranging from 0-255
   *
   * @param green as integer
   */
  public Color setGreen(int green) {
    return set(getRed(), green, getBlue(), getAlpha());
  }
  
  /**
   * Set blue ranging from 0-255
   *
   * @param blue as integer
   */
  public Color setBlue(int blue) {
    return set(getRed(), getGreen(), blue, getAlpha());
  }
  
  /**
   * Set alpha ranging from 0-255
   *
   * @param alpha as integer
   */
  public Color setAlpha(int alpha) {
    return set(getRed(), getGreen(), getBlue(), alpha);
  }
  
  /**
   * Red color ranging from 0-1
   *
   * @return red as float
   */
  public abstract float getRedAsFloat();
  
  /**
   * Green color ranging from 0-1
   *
   * @return green as float
   */
  public abstract float getGreenAsFloat();
  
  /**
   * Blue color ranging from 0-1
   *
   * @return blue as float
   */
  public abstract float getBlueAsFloat();
  
  /**
   * Alpha color ranging from 0-1
   *
   * @return alpha as float
   */
  public abstract float getAlphaAsFloat();
  
  /**
   * Set red ranging from 0-1
   *
   * @param red as float
   */
  public Color setRed(float red) {
    return set(red, getGreenAsFloat(), getBlueAsFloat(), getAlphaAsFloat());
  }
  
  /**
   * Set green ranging from 0-1
   *
   * @param green as float
   */
  public Color setGreen(float green) {
    return set(getRedAsFloat(), green, getBlueAsFloat(), getAlphaAsFloat());
  }
  
  /**
   * Set blue ranging from 0-1
   *
   * @param blue as float
   */
  public Color setBlue(float blue) {
    return set(getRedAsFloat(), getGreenAsFloat(), blue, getAlphaAsFloat());
  }
  
  /**
   * Set alpha ranging from 0-1
   *
   * @param alpha as float
   */
  public Color setAlpha(float alpha) {
    return set(getRedAsFloat(), getGreenAsFloat(), getBlueAsFloat(), alpha);
  }
  
  /**
   * Red color ranging from 0-1 NOTE: double is overkill for color, this is just a convenience
   * method that returns a float casted as a double
   *
   * @return red as float casted as a double
   */
  public double getRedAsDouble() {
    return getRedAsFloat();
  }
  
  /**
   * Green color ranging from 0-1 NOTE: double is overkill for color, this is just a convenience
   * method that returns a float casted as a double
   *
   * @return green as float casted as a double
   */
  public double getGreenAsDouble() {
    return getGreenAsFloat();
  }
  
  /**
   * Blue color ranging from 0-1 NOTE: double is overkill for color, this is just a convenience
   * method that returns a float casted as a double
   *
   * @return blue as float casted as a double
   */
  public double getBlueAsDouble() {
    return getBlueAsFloat();
  }
  
  /**
   * Alpha color ranging from 0-1 NOTE: double is overkill for color, this is just a convenience
   * method that returns a float casted as a double
   *
   * @return alpha as float casted as a double
   */
  public double getAlphaAsDouble() {
    return getAlphaAsFloat();
  }
  
  /**
   * Set red ranging from 0-1 NOTE: double is overkill for color, this is just a convenience method
   * that will cast the argument as a double
   *
   * @param red as double (will be casted to float)
   */
  public Color setRed(double red) {
    return setRed((float) red);
  }
  
  /**
   * Set green ranging from 0-1 NOTE: double is overkill for color, this is just a convenience
   * method that will cast the argument as a double
   *
   * @param green as double (will be casted to float)
   */
  public Color setGreen(double green) {
    return setGreen((float) green);
  }
  
  /**
   * Set blue ranging from 0-1 NOTE: double is overkill for color, this is just a convenience method
   * that will cast the argument as a double
   *
   * @param blue as double (will be casted to float)
   */
  public Color setBlue(double blue) {
    return setBlue((float) blue);
  }
  
  /**
   * Set alpha ranging from 0-1 NOTE: double is overkill for color, this is just a convenience
   * method that will cast the argument as a double
   *
   * @param alpha as double (will be casted to float)
   */
  public Color setAlpha(double alpha) {
    return setAlpha((float) alpha);
  }
  
  /**
   * Gets the color as an integer buffer.
   *
   * @return integer representing the color
   */
  public int toBuffer() {
    return (getRed() << 16) + (getGreen() << 8) + getBlue() + (getAlpha() << 24);
  }
  
  /**
   * Converts the color to a integer array {r,g,b,a}
   *
   * @return integer array containing the color
   */
  public int[] toIntegerArray() {
    return new int[]{getRed(), getGreen(), getBlue(), getAlpha()};
  }
  
  /**
   * Converts the color to a float array {r,g,b,a}
   *
   * @return float array containing the color
   */
  public float[] toFloatArray() {
    return new float[]{getRedAsFloat(), getGreenAsFloat(), getBlueAsFloat(), getAlphaAsFloat()};
  }
  
  /**
   * Converts the color to a double array {r,g,b,a} NOTE: double is overkill for color, this is just
   * a convenience method
   *
   * @return double array containing the color (contents are all floats casted as doubles)
   */
  public double[] toDoubleArray() {
    float[] array = toFloatArray();
    return new double[]{array[0], array[1], array[2], array[3]};
  }

  public void glSetColor4f() {
    RenderSystem.color4f(getRedAsFloat(), getGreenAsFloat(), getBlueAsFloat(), getAlphaAsFloat());
  }

  public void glSetColor3f() {
    RenderSystem.color3f(getRedAsFloat(), getGreenAsFloat(), getBlueAsFloat());
  }

  public abstract boolean isBufferType();

  public final boolean isFloatType() {
    return !isBufferType();
  }
  
  @Override
  public String toString() {
    return getRed() + " " + getGreen() + " " + getBlue() + " " + getAlpha();
  }
  
  @Override
  public int hashCode() {
    return toBuffer();
  }
  
  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof Color && toBuffer() == ((Color) obj).toBuffer());
  }
}
