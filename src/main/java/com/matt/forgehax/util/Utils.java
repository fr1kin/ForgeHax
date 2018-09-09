package com.matt.forgehax.util;

import com.matt.forgehax.Globals;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.math.VectorUtils;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils implements Globals {

    /**
     * Use PacketHelper class now
     */
    @Deprecated
    public static final List<Packet> OUTGOING_PACKET_IGNORE_LIST = Collections.emptyList();

    @Deprecated
    public static int toRGBA(int r, int g, int b, int a) {
        return (r << 16) + (g << 8) + (b << 0) + (a << 24);
    }

    @Deprecated
    public static int toRGBA(float r, float g, float b, float a) {
        return toRGBA((int) (r * 255.f), (int) (g * 255.f), (int) (b * 255.f), (int) (a * 255.f));
    }
    @Deprecated
    public static int toRGBA(float[] colors) {
        if(colors.length != 4) throw new IllegalArgumentException("colors[] must have a length of 4!");
        return toRGBA(colors[0], colors[1], colors[2], colors[3]);
    }
    @Deprecated
    public static int toRGBA(double[] colors) {
        if(colors.length != 4) throw new IllegalArgumentException("colors[] must have a length of 4!");
        return toRGBA((float)colors[0], (float)colors[1], (float)colors[2], (float)colors[3]);
    }

    @Deprecated
    public static int[] toRGBAArray(int colorBuffer) {
        return new int[] {
                (colorBuffer >> 16 & 255),
                (colorBuffer >> 8 & 255),
                (colorBuffer & 255),
                (colorBuffer >> 24 & 255)
        };
    }

    public static <E extends Enum<?>> String[] toArray(E[] o) {
        String[] output = new String[o.length];
        for(int i = 0; i < output.length; i++)
            output[i] = o[i].name();
        return output;
    }

    public static UUID stringToUUID(String uuid) {
        if(uuid.contains("-")) {
            // if it contains the hyphen we don't have to manually put them in
            return UUID.fromString(uuid);
        } else {
            // otherwise we have to put
            Pattern pattern = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
            Matcher matcher = pattern.matcher(uuid);
            return UUID.fromString(matcher.replaceAll("$1-$2-$3-$4-$5"));
        }
    }

    public static double normalizeAngle(double angle) {
        while (angle <= -180) angle += 360;
        while (angle > 180) angle -= 360;
        return angle;
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static Angle getLookAtAngles(Vec3d startPos, Vec3d endPos) {
        return VectorUtils.vectorAngle(endPos.subtract(startPos)).normalize();
    }
    public static Angle getLookAtAngles(Vec3d endPos) {
        return getLookAtAngles(EntityUtils.getEyePos(MC.player), endPos);
    }
    public static Angle getLookAtAngles(Entity entity) {
        return getLookAtAngles(EntityUtils.getOBBCenter(entity));
    }

    public static double scale(double x, double from_min, double from_max, double to_min, double to_max) {
        return to_min + (to_max - to_min) * ((x - from_min) / (from_max - from_min));
    }

    public static <T> boolean isInRange(T[] array, int index) {
        return array != null && index >= 0 && index < array.length;
    }

    public static <T> boolean isInRange(List<T> list, int index) {
        return list != null && index >= 0 && index < list.size();
    }

    public static <T> T defaultTo(T value, T defaultTo) {
        return value == null ? defaultTo : value;
    }

    @Deprecated
    public static class Colors {
        public final static int WHITE           = Utils.toRGBA(255,     255,    255,    255);
        public final static int BLACK           = Utils.toRGBA(0,       0,      0,      255);
        public final static int RED             = Utils.toRGBA(255,     0,      0,      255);
        public final static int GREEN           = Utils.toRGBA(0,       255,    0,      255);
        public final static int BLUE            = Utils.toRGBA(0,       0,      255,    255);
        public final static int ORANGE          = Utils.toRGBA(255,     128,    0,      255);
        public final static int PURPLE          = Utils.toRGBA(163,     73,     163,    255);
        public final static int GRAY            = Utils.toRGBA(127,     127,    127,    255);
        public final static int DARK_RED        = Utils.toRGBA(64,      0,      0,      255);
        public final static int YELLOW          = Utils.toRGBA(255,     255,    0,      255);
    }
}
