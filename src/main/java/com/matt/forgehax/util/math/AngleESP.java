package com.matt.forgehax.util.math;

import com.matt.forgehax.util.Utils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Thanks LuaStoned */
public class AngleESP {
  private double p = 0, y = 0, r = 0;

  public AngleESP() {
    p = y = r = 0.D;
  }

  public AngleESP(double p, double y, double r) {
    this.p = p;
    this.y = y;
    this.r = r;
  }

  public AngleESP(double p, double y) {
    this.p = p;
    this.y = y;
  }

  public AngleESP(AngleESP copy) {
    this.p = copy.p;
    this.y = copy.y;
    this.r = copy.r;
  }

  public boolean equals(AngleESP other) {
    if (p != other.p) return false;
    if (y != other.y) return false;
    if (r != other.r) return false;
    return false;
  }

  public AngleESP add(AngleESP other) {
    p += other.p;
    y += other.y;
    r += other.r;
    return this;
  }

  public AngleESP sub(AngleESP other) {
    p -= other.p;
    y -= other.y;
    r -= other.r;
    return this;
  }

  public AngleESP mul(AngleESP other) {
    p *= other.p;
    y *= other.y;
    r *= other.r;
    return this;
  }

  public AngleESP mul(double other) {
    p *= other;
    y *= other;
    r *= other;
    return this;
  }

  public AngleESP div(AngleESP other) {
    p /= other.p;
    y /= other.y;
    r /= other.r;
    return this;
  }

  public AngleESP div(double other) {
    p /= other;
    y /= other;
    r /= other;
    return this;
  }

  public Vec3d forward() {
    float pitch = (float) Math.toRadians(p);
    float yaw = (float) Math.toRadians(y);
    float roll = (float) Math.toRadians(r);

    double x =
        (MathHelper.sin(roll) * MathHelper.sin(pitch))
            + (MathHelper.cos(roll) * MathHelper.sin(yaw) * MathHelper.cos(pitch));
    double y =
        (-MathHelper.cos(roll) * MathHelper.sin(pitch))
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

    double x =
        (-MathHelper.sin(roll) * MathHelper.cos(pitch))
            + (MathHelper.cos(roll) * MathHelper.sin(yaw) * MathHelper.sin(pitch));
    double y =
        (MathHelper.cos(roll) * MathHelper.cos(pitch))
            + (MathHelper.sin(roll) * MathHelper.sin(yaw) * MathHelper.sin(pitch));
    double z = MathHelper.cos(yaw) * MathHelper.sin(pitch);

    return new Vec3d(x, y, z);
  }

  public AngleESP normalize() {
    return new AngleESP(Utils.normalizeAngle(p), Utils.normalizeAngle(y), Utils.normalizeAngle(r));
  }

  // convert polar coords to cartesian coords
  public Vec3d getCartesianCoords() {
    double c = Math.cos(getPitch(true));
    return new Vec3d(
        Math.cos(getYaw(true)) * c, Math.sin(getPitch(true)), Math.sin(getYaw(true)) * c);
  }

  public double getPitch(boolean inRadians) {
    return inRadians ? Math.toRadians(p) : p;
  }

  public double getPitch() {
    return getPitch(false);
  }

  public double getYaw(boolean inRadians) {
    return inRadians ? Math.toRadians(y) : y;
  }

  public double getYaw() {
    return getYaw(false);
  }

  public double getRoll(boolean inRadians) {
    return inRadians ? Math.toRadians(r) : r;
  }

  public double getRoll() {
    return getRoll(false);
  }

  public void setPitch(double p) {
    this.p = p;
  }

  public void setYaw(double y) {
    this.y = y;
  }

  public void setRoll(double r) {
    this.r = r;
  }

  @Override
  public String toString() {
    return String.format("(p, y, r) = %.2f, %.2f, %.2f", p, y, r);
  }
}
