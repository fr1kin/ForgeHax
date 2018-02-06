package com.matt.forgehax.util.color;

import java.util.Arrays;

/**
 * Created on 2/6/2018 by fr1kin
 */
public class Color4F extends Color {
    private final float[] color = new float[4];

    protected Color4F() {}
    private Color4F(Color4F other) {
        set(other.color);
    }

    @Override
    public void set(int buffer) {
        set(
                (float)(buffer >> 16 & 255) / 255.f,
                (float)(buffer >> 8 & 255) / 255.f,
                (float)(buffer & 255) / 255.f,
                (float)(buffer >> 24 & 255) / 255.f
        );
    }

    @Override
    public void set(float red, float green, float blue, float alpha) {
        color[0] = red;
        color[1] = green;
        color[2] = blue;
        color[3] = alpha;
    }

    @Override
    public int getRed() {
        return (int)(getRedAsFloat() * 255);
    }

    @Override
    public int getGreen() {
        return (int)(getRedAsFloat() * 255);
    }

    @Override
    public int getBlue() {
        return (int)(getRedAsFloat() * 255);
    }

    @Override
    public int getAlpha() {
        return (int)(getRedAsFloat() * 255);
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
    public Color toImmutable() {
        return new Immutable(this);
    }

    @Override
    public Color copy() {
        return new Color4F(this);
    }

    @Override
    public String toString() {
        return String.format("r=%.2f,g=%.2f,b=%.2f,a=%.2f", getRedAsFloat(), getGreenAsFloat(), getBlueAsFloat(), getAlphaAsFloat());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(color);
    }

    private static class Immutable extends Color4F {
        public Immutable(Color4F other) {
            super(other);
        }

        @Override
        public void set(float red, float green, float blue, float alpha) {
            throw new UnsupportedOperationException("Color has been made immutable");
        }
    }
}
