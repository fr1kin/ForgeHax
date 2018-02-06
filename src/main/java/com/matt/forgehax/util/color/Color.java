package com.matt.forgehax.util.color;

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

    public static Color newColorBuffer() {
        return new ColorBuffer();
    }

    public static Color newColor4F() {
        return new Color4F();
    }

    public static Color of(int buffer) {
        Color c = newColorBuffer();
        c.set(buffer);
        return c;
    }
    public static Color of(int red, int green, int blue, int alpha) {
        Color c = newColorBuffer();
        c.set(red, green, blue, alpha);
        return c;
    }
    public static Color of(int red, int green, int blue) {
        Color c = newColorBuffer();
        c.set(red, green, blue);
        return c;
    }
    public static Color of(int[] color) {
        Color c = newColorBuffer();
        c.set(color);
        return c;
    }

    public static Color of(float red, float green, float blue, float alpha) {
        Color c = newColor4F();
        c.set(red, green, blue, alpha);
        return c;
    }
    public static Color of(float red, float green, float blue) {
        Color c = newColor4F();
        c.set(red, green, blue);
        return c;
    }
    public static Color of(float[] color) {
        Color c = newColor4F();
        c.set(color);
        return c;
    }

    public static Color of(double red, double green, double blue, double alpha) {
        Color c = newColor4F();
        c.set(red, green, blue, alpha);
        return c;
    }
    public static Color of(double red, double green, double blue) {
        Color c = newColor4F();
        c.set(red, green, blue);
        return c;
    }
    public static Color of(double[] color) {
        Color c = newColor4F();
        c.set(color);
        return c;
    }

    //
    //
    //

    /**
     * Set color by buffer
     * @param buffer color buffer
     */
    public abstract void set(int buffer);

    /**
     * Set the RGBA accordingly
     * @param red as integer 0-255
     * @param green as integer 0-255
     * @param blue as integer 0-255
     * @param alpha as integer 0-255
     */
    public void set(int red, int green, int blue, int alpha) {
        set((red << 16) + (green << 8) + blue + (alpha << 24));
    }
    /**
     * Set the RGB accordingly, using 255 (maximum) for the alpha
     * @param red as integer 0-255
     * @param green as integer 0-255
     * @param blue as integer 0-255
     */
    public void set(int red, int green, int blue) {
        set(red, green, blue, 255);
    }

    public void set(int[] color) {
        Objects.requireNonNull(color);
        switch (color.length) {
            case 3:
                set(color[0], color[1], color[2]);
                break;
            case 4:
                set(color[0], color[1], color[2], color[3]);
                break;
            default: throw new IllegalArgumentException("color[] must be of length 3 or 4");
        }
    }

    /**
     * Set the RGBA accordingly
     * @param red as float 0-1
     * @param green as float 0-1
     * @param blue as float 0-1
     * @param alpha as float 0-1
     */
    public abstract void set(float red, float green, float blue, float alpha);
    /**
     * Set the RGBA accordingly, using 1.f (maximum) for the alpha
     * @param red as float 0-1
     * @param green as float 0-1
     * @param blue as float 0-1
     */
    public void set(float red, float green, float blue) {
        set(red, green, blue, 1.f);
    }
    public void set(float[] color) {
        Objects.requireNonNull(color);
        switch (color.length) {
            case 3:
                set(color[0], color[1], color[2]);
                break;
            case 4:
                set(color[0], color[1], color[2], color[3]);
                break;
            default: throw new IllegalArgumentException("color[] must be of length 3 or 4");
        }
    }

    /**
     * Set the RGBA accordingly
     * Will cast all arguments to floats and use set(ffff)
     * @param red as double 0-1
     * @param green as double 0-1
     * @param blue as double 0-1
     * @param alpha as double 0-1
     */
    public void set(double red, double green, double blue, double alpha) {
        set((float)red, (float)green, (float)blue, (float)alpha);
    }
    /**
     * Set the RGBA accordingly, using 1.D (maximum) for the alpha
     * Will cast all arguments to floats and use set(ffff)
     * @param red as double 0-1
     * @param green as double 0-1
     * @param blue as double 0-1
     */
    public void set(double red, double green, double blue) {
        set(red, green, blue, 1.D);
    }
    public void set(double[] color) {
        Objects.requireNonNull(color);
        switch (color.length) {
            case 3:
                set(color[0], color[1], color[2]);
                break;
            case 4:
                set(color[0], color[1], color[2], color[3]);
                break;
            default: throw new IllegalArgumentException("color[] must be of length 3 or 4");
        }
    }

    /**
     * Red color ranging from 0-255
     * @return red as integer
     */
    public abstract int getRed();
    /**
     * Green color ranging from 0-255
     * @return green as integer
     */
    public abstract int getGreen();
    /**
     * Blue color ranging from 0-255
     * @return blue as integer
     */
    public abstract int getBlue();
    /**
     * Alpha color ranging from 0-255
     * @return alpha as integer
     */
    public abstract int getAlpha();

    /**
     * Set red ranging from 0-255
     * @param red as integer
     */
    public void setRed(int red) {
        set(red, getGreen(), getBlue(), getAlpha());
    }
    /**
     * Set green ranging from 0-255
     * @param green as integer
     */
    public void setGreen(int green) {
        set(getRed(), green, getBlue(), getAlpha());
    }
    /**
     * Set blue ranging from 0-255
     * @param blue as integer
     */
    public void setBlue(int blue) {
        set(getRed(), getGreen(), blue, getAlpha());
    }
    /**
     * Set alpha ranging from 0-255
     * @param alpha as integer
     */
    public void setAlpha(int alpha) {
        set(getRed(), getGreen(), getBlue(), alpha);
    }

    /**
     * Red color ranging from 0-1
     * @return red as float
     */
    public abstract float getRedAsFloat();
    /**
     * Green color ranging from 0-1
     * @return green as float
     */
    public abstract float getGreenAsFloat();
    /**
     * Blue color ranging from 0-1
     * @return blue as float
     */
    public abstract float getBlueAsFloat();
    /**
     * Alpha color ranging from 0-1
     * @return alpha as float
     */
    public abstract float getAlphaAsFloat();

    /**
     * Set red ranging from 0-1
     * @param red as float
     */
    public void setRed(float red) {
        set(red, getGreenAsFloat(), getBlueAsFloat(), getAlphaAsFloat());
    }
    /**
     * Set green ranging from 0-1
     * @param green as float
     */
    public void setGreen(float green) {
        set(getRedAsFloat(), green, getBlueAsFloat(), getAlphaAsFloat());
    }
    /**
     * Set blue ranging from 0-1
     * @param blue as float
     */
    public void setBlue(float blue) {
        set(getRedAsFloat(), getGreenAsFloat(), blue, getAlphaAsFloat());
    }
    /**
     * Set alpha ranging from 0-1
     * @param alpha as float
     */
    public void setAlpha(float alpha) {
        set(getRedAsFloat(), getGreenAsFloat(), getBlueAsFloat(), alpha);
    }

    /**
     * Red color ranging from 0-1
     * NOTE: double is overkill for color, this is just a convenience method that returns a float casted as a double
     * @return red as float casted as a double
     */
    public double getRedAsDouble() {
        return getRedAsFloat();
    }
    /**
     * Green color ranging from 0-1
     * NOTE: double is overkill for color, this is just a convenience method that returns a float casted as a double
     * @return green as float casted as a double
     */
    public double getGreenAsDouble() {
        return getGreenAsFloat();
    }
    /**
     * Blue color ranging from 0-1
     * NOTE: double is overkill for color, this is just a convenience method that returns a float casted as a double
     * @return blue as float casted as a double
     */
    public double getBlueAsDouble() {
        return getBlueAsFloat();
    }
    /**
     * Alpha color ranging from 0-1
     * NOTE: double is overkill for color, this is just a convenience method that returns a float casted as a double
     * @return alpha as float casted as a double
     */
    public double getAlphaAsDouble() {
        return getAlphaAsFloat();
    }

    /**
     * Set red ranging from 0-1
     * NOTE: double is overkill for color, this is just a convenience method that will cast the argument as a double
     * @param red as double (will be casted to float)
     */
    public void setRed(double red) {
        setRed((float)red);
    }
    /**
     * Set green ranging from 0-1
     * NOTE: double is overkill for color, this is just a convenience method that will cast the argument as a double
     * @param green as double (will be casted to float)
     */
    public void setGreen(double green) {
        setGreen((float)green);
    }
    /**
     * Set blue ranging from 0-1
     * NOTE: double is overkill for color, this is just a convenience method that will cast the argument as a double
     * @param blue as double (will be casted to float)
     */
    public void setBlue(double blue) {
        setBlue((float)blue);
    }
    /**
     * Set alpha ranging from 0-1
     * NOTE: double is overkill for color, this is just a convenience method that will cast the argument as a double
     * @param alpha as double (will be casted to float)
     */
    public void setAlpha(double alpha) {
        setAlpha((float)alpha);
    }

    /**
     * Gets the color as an integer buffer.
     * @return integer representing the color
     */
    public int toBuffer() {
        return (getRed() << 16) + (getGreen() << 8) + getBlue() + (getAlpha() << 24);
    }

    /**
     * Converts the color to a integer array {r,g,b,a}
     * @return integer array containing the color
     */
    public int[] toIntegerArray() {
        return new int[] {getRed(), getGreen(), getBlue(), getAlpha()};
    }

    /**
     * Converts the color to a float array {r,g,b,a}
     * @return float array containing the color
     */
    public float[] toFloatArray() {
        return new float[] {getRedAsFloat(), getGreenAsFloat(), getBlueAsFloat(), getAlphaAsFloat()};
    }

    /**
     * Converts the color to a double array {r,g,b,a}
     * NOTE: double is overkill for color, this is just a convenience method
     * @return double array containing the color (contents are all floats casted as doubles)
     */
    public double[] toDoubleArray() {
        float[] array = toFloatArray();
        return new double[] {array[0], array[1], array[2], array[3]};
    }

    /**
     * Creates a color instance that's setters
     * @return immutable color instance
     */
    public abstract Color toImmutable();

    /**
     * Will copy this instance into another instance.
     * Instance will be mutable even if toImmutable() was used to make the parent class.
     * @return a mutable new instance of the object
     */
    public abstract Color copy();

    @Override
    public abstract String toString();

    @Override
    public abstract int hashCode();

    @Override
    public boolean equals(Object obj) {
        return this == obj
                || (obj instanceof Color && hashCode() == obj.hashCode());
    }
}
