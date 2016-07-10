package com.matt.forgehax.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Thanks LuaStoned
 */
public class Angle {
    public double p = 0, y = 0, r = 0;

    public Angle(double p, double y, double r) {
        this.p = p;
        this.y = y;
        this.r = r;
    }
    public Angle(double p, double y) {
        this.p = p;
        this.y = y;
    }
    public Angle(Angle copy) {
        this.p = copy.p;
        this.y = copy.y;
        this.r = copy.r;
    }

    public boolean equals(Angle other) {
        if (p != other.p)
            return false;
        if (y != other.y)
            return false;
        if (r != other.r)
            return false;
        return false;
    }

    public Angle add(Angle other) {
        p += other.p;
        y += other.y;
        r += other.r;
        return this;
    }

    public Angle sub(Angle other) {
        p -= other.p;
        y -= other.y;
        r -= other.r;
        return this;
    }

    public Angle mul(Angle other) {
        p *= other.p;
        y *= other.y;
        r *= other.r;
        return this;
    }

    public Angle mul(double other) {
        p *= other;
        y *= other;
        r *= other;
        return this;
    }

    public Angle div(Angle other) {
        p /= other.p;
        y /= other.y;
        r /= other.r;
        return this;
    }

    public Angle div(double other) {
        p /= other;
        y /= other;
        r /= other;
        return this;
    }

    public Vec3d forward() {
        float pitch = (float) Math.toRadians(p);
        float yaw = (float) Math.toRadians(y);
        float roll = (float) Math.toRadians(r);

        double x = (MathHelper.sin(roll) * MathHelper.sin(pitch))
                + (MathHelper.cos(roll) * MathHelper.sin(yaw) * MathHelper.cos(pitch));
        double y = (-MathHelper.cos(roll) * MathHelper.sin(pitch))
                + (MathHelper.sin(roll) * MathHelper.sin(yaw) * MathHelper.cos(pitch));
        double z = MathHelper.cos(yaw) * MathHelper.cos(pitch);

        return new Vec3d(x, y, z);
    }

    public Vec3d right() {
        float yaw = (float) Math.toRadians(y);
        float roll = (float) Math.toRadians(r);

        double x = MathHelper.cos(roll) * MathHelper.cos(yaw);
        double y = MathHelper.sin(roll) * MathHelper.cos(yaw);
        double z = -MathHelper.sin(yaw);

        return new Vec3d(x, y, z);
    }

    public Vec3d up() {
        float pitch = (float) Math.toRadians(p);
        float yaw = (float) Math.toRadians(y);
        float roll = (float) Math.toRadians(r);

        double x = (-MathHelper.sin(roll) * MathHelper.cos(pitch))
                + (MathHelper.cos(roll) * MathHelper.sin(yaw) * MathHelper.sin(pitch));
        double y = (MathHelper.cos(roll) * MathHelper.cos(pitch))
                + (MathHelper.sin(roll) * MathHelper.sin(yaw) * MathHelper.sin(pitch));
        double z = MathHelper.cos(yaw) * MathHelper.sin(pitch);

        return new Vec3d(x, y, z);
    }

    public Angle normalize() {
        return new Angle(
                Utils.normalizeAngle(p),
                Utils.normalizeAngle(y),
                Utils.normalizeAngle(r)
        );
    }
}
