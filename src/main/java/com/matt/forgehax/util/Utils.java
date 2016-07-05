package com.matt.forgehax.util;

import com.matt.forgehax.ForgeHaxBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class Utils extends ForgeHaxBase {
    public static int toRGBA(int r, int g, int b, int a) {
        return (r << 16) + (g << 8) + (b << 0) + (a << 24);
    }

    public static int toRGBA(float r, float g, float b, float a) {
        return toRGBA((int) (r * 255.f), (int) (g * 255.f), (int) (b * 255.f), (int) (a * 255.f));
    }

    public static <E extends Enum<?>> String[] toArray(E[] o) {
        String[] output = new String[o.length];
        for(int i = 0; i < output.length; i++)
            output[i] = o[i].name();
        return output;
    }
}
