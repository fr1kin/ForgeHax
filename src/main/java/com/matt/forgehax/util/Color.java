package com.matt.forgehax.util;

/**
 * Created on 9/15/2017 by fr1kin
 */
public class Color {
    private final int color;

    //(r << 16) + (g << 8) + (b << 0) + (a << 24);

    private Color(int color) {
        this.color = color;
    }

    public int getAsBuffer() {
        return color;
    }

    public int getRed() {
        return color >> 16 & 255;
    }
    public int getGreen() {
        return color >> 8 & 255;
    }
    public int getBlue() {
        return color & 255;
    }
    public int getAlpha() {
        return color >> 24 & 255;
    }

    public float getRedAsFloat() {
        return getRed() / 255.f;
    }
    public float getGreenAsFloat() {
        return getGreen() / 255.f;
    }
    public float getBlueAsFloat() {
        return getBlue() / 255.f;
    }
    public float getAlphaAsFloat() {
        return getAlpha() / 255.f;
    }

    public double getRedAsDouble() {
        return getRed() / 255.D;
    }
    public double getGreenAsDouble() {
        return getGreen() / 255.D;
    }
    public double getBlueAsDouble() {
        return getBlue() / 255.D;
    }
    public double getAlphaAsDouble() {
        return getAlpha() / 255.D;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj
                || obj instanceof Color && color == ((Color) obj).color;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(color);
    }
}
