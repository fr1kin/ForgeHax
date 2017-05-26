package com.matt.forgehax.util.blocks.properties;

import com.google.gson.JsonObject;
import com.matt.forgehax.util.Utils;
import net.minecraft.util.math.MathHelper;

/**
 * Created on 5/20/2017 by fr1kin
 */
public class BlockColorProperty implements IBlockProperty {
    private static final String HEADING = "color";
    private static final int DEFAULT_COLOR = Utils.Colors.WHITE;

    private int r;
    private int g;
    private int b;
    private int a;

    private int buffer;

    public BlockColorProperty() {
        set(DEFAULT_COLOR);
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

    public boolean isDefaultColor() {
        return buffer == DEFAULT_COLOR;
    }

    @Override
    public void serialize(JsonObject head) {
        if(!isDefaultColor()) head.addProperty(HEADING, buffer);
    }

    @Override
    public void deserialize(JsonObject head) {
        if(head.has(HEADING)) try {
            set(head.get(HEADING).getAsInt());
        } catch (Exception e) {
            ;
        }
    }

    @Override
    public String toString() {
        return String.format("%s=(%d, %d, %d, %d)", HEADING, r, g, b, a);
    }
}
