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

    public static double normalizeAngle(double angle) {
        while (angle <= -180) angle += 360;
        while (angle > 180) angle -= 360;
        return angle;
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static class Colors {
        public final static int WHITE           = Utils.toRGBA(255,     255,    255,    255);
        public final static int BLACK           = Utils.toRGBA(0,       0,      0,      255);
        public final static int RED             = Utils.toRGBA(255,     0,      0,      255);
        public final static int GREEN           = Utils.toRGBA(0,       255,    0,      255);
        public final static int BLUE            = Utils.toRGBA(0,       0,      255,    255);
        public final static int ORANGE          = Utils.toRGBA(255,     128,    0,      255);
    }
}
