package com.matt.forgehax.util.blocks.properties;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.Utils;
import java.io.IOException;
import net.minecraft.util.math.MathHelper;

/** Created on 5/20/2017 by fr1kin */
public class ColorProperty implements IBlockProperty {
  private static final String HEADING = "color";
  private static final int DEFAULT_COLOR_BUFFER = Utils.Colors.WHITE;
  private static final int[] DEFAULT_COLOR_ARRAY = Utils.toRGBAArray(DEFAULT_COLOR_BUFFER);

  private int r;
  private int g;
  private int b;
  private int a;

  private int buffer;

  public ColorProperty() {
    set(DEFAULT_COLOR_BUFFER);
  }

  public int getRed() {
    return r;
  }

  public int getGreen() {
    return g;
  }

  public int getBlue() {
    return b;
  }

  public int getAlpha() {
    return a;
  }

  public int[] getAsArray() {
    return new int[] {r, g, b, a};
  }

  public int getAsBuffer() {
    return buffer;
  }

  public void set(int r, int g, int b, int a) {
    this.r = MathHelper.clamp(r, 0, 255);
    this.g = MathHelper.clamp(g, 0, 255);
    this.b = MathHelper.clamp(b, 0, 255);
    this.a = MathHelper.clamp(a, 0, 255);
    this.buffer = Utils.toRGBA(this.r, this.g, this.b, this.a);
  }

  public void set(int buffer) {
    int[] rgba = Utils.toRGBAArray(buffer);
    set(rgba[0], rgba[1], rgba[2], rgba[3]);
  }

  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.value(buffer);
  }

  @Override
  public void deserialize(JsonReader reader) throws IOException {
    set(reader.nextInt());
  }

  @Override
  public boolean isNecessary() {
    return buffer != DEFAULT_COLOR_BUFFER;
  }

  @Override
  public String helpText() {
    return String.format("(%d, %d, %d, %d)", r, g, b, a);
  }

  @Override
  public IBlockProperty newImmutableInstance() {
    return new ImmutableColor();
  }

  @Override
  public String toString() {
    return HEADING;
  }

  private static class ImmutableColor extends ColorProperty {
    @Override
    public int getRed() {
      return DEFAULT_COLOR_ARRAY[0];
    }

    @Override
    public int getGreen() {
      return DEFAULT_COLOR_ARRAY[1];
    }

    @Override
    public int getBlue() {
      return DEFAULT_COLOR_ARRAY[2];
    }

    @Override
    public int getAlpha() {
      return DEFAULT_COLOR_ARRAY[3];
    }

    @Override
    public int getAsBuffer() {
      return DEFAULT_COLOR_BUFFER;
    }

    @Override
    public int[] getAsArray() {
      return DEFAULT_COLOR_ARRAY;
    }

    @Override
    public void set(int buffer) {}

    @Override
    public void set(int r, int g, int b, int a) {}
  }
}
