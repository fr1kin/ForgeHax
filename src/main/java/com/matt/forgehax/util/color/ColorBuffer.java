package com.matt.forgehax.util.color;

/**
 * Created on 2/5/2018 by fr1kin
 */
public class ColorBuffer extends Color {
    private int buffer = 0;

    protected ColorBuffer() {}
    private ColorBuffer(ColorBuffer other) {
        set(other.buffer);
    }

    @Override
    public void set(int buffer) {
        this.buffer = buffer;
    }

    @Override
    public void set(float red, float green, float blue, float alpha) {
        set((int)(red * 255.f), (int)(green * 255.f), (int)(blue * 255.f), (int)(alpha * 255.f));
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
        return (float)getRed() / 255.f;
    }

    @Override
    public float getGreenAsFloat() {
        return (float)getGreen() / 255.f;
    }

    @Override
    public float getBlueAsFloat() {
        return (float)getBlue() / 255.f;
    }

    @Override
    public float getAlphaAsFloat() {
        return (float)getAlpha() / 255.f;
    }

    @Override
    public int toBuffer() {
        return buffer;
    }

    @Override
    public float[] toFloatArray() {
        return new float[] {getRedAsFloat(), getGreenAsFloat(), getBlueAsFloat(), getAlphaAsFloat()};
    }

    @Override
    public Color toImmutable() {
        return new Immutable(this);
    }

    @Override
    public Color copy() {
        return new ColorBuffer(this);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(buffer);
    }

    @Override
    public String toString() {
        return String.format("r=%d,g=%d,b=%d,a=%d", getRed(), getGreen(), getBlue(), getAlpha());
    }

    private static class Immutable extends ColorBuffer {
        private Immutable(ColorBuffer other) {
            super(other);
        }

        @Override
        public void set(int buffer) {
            throw new UnsupportedOperationException("Color has been made immutable");
        }
    }
}
