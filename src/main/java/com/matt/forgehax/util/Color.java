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

    public float getRedf() {
        return (float)getRed() / 255.f;
    }
    public float getGreenf() {
        return (float)getGreen() / 255.f;
    }
    public float getBluef() {
        return (float)getBlue() / 255.f;
    }
    public float getAlphaf() {
        return (float)getAlpha() / 255.f;
    }

    public double getRedd() {
        return (double)getRed() / 255.D;
    }
    public double getGreend() {
        return (double)getGreen() / 255.D;
    }
    public double getBlued() {
        return (double)getBlue() / 255.D;
    }
    public double getAlphad() {
        return (double)getAlpha() / 255.D;
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
