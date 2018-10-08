package com.matt.forgehax.util.math;

import java.util.Objects;

/**
 * Created on 6/21/2017 by fr1kin
 *
 * <p>TODO: replace Angle.java with this
 */
public abstract class AngleN {
  public static final AngleN ZERO = degrees(0.f, 0.f, 0.f);

  public static AngleN radians(float pitch, float yaw, float roll) {
    return new Radians(pitch, yaw, roll);
  }

  public static AngleN radians(float pitch, float yaw) {
    return radians(pitch, yaw, 0.f);
  }

  public static AngleN radians(double pitch, double yaw, double roll) {
    return radians(
        (float) AngleHelper.roundAngle(pitch),
        (float) AngleHelper.roundAngle(yaw),
        (float) AngleHelper.roundAngle(roll));
  }

  public static AngleN radians(double pitch, double yaw) {
    return radians(pitch, yaw, 0.D);
  }

  public static AngleN degrees(float pitch, float yaw, float roll) {
    return new Degrees(pitch, yaw, roll);
  }

  public static AngleN degrees(float pitch, float yaw) {
    return degrees(pitch, yaw, 0.f);
  }

  public static AngleN degrees(double pitch, double yaw, double roll) {
    return degrees(
        (float) AngleHelper.roundAngle(pitch),
        (float) AngleHelper.roundAngle(yaw),
        (float) AngleHelper.roundAngle(roll));
  }

  public static AngleN degrees(double pitch, double yaw) {
    return degrees(pitch, yaw, 0.D);
  }

  public static AngleN copy(AngleN ang) {
    return ang.newInstance(ang.getPitch(), ang.getYaw(), ang.getRoll());
  }

  private final float pitch;
  private final float yaw;
  private final float roll;

  private AngleN(float pitch, float yaw, float roll) {
    this.pitch = pitch;
    this.yaw = yaw;
    this.roll = roll;
  }

  public float getPitch() {
    return pitch;
  }

  public float getYaw() {
    return yaw;
  }

  public float getRoll() {
    return roll;
  }

  public AngleN setPitch(float pitch) {
    return newInstance(pitch, getYaw(), getRoll());
  }

  public AngleN setYaw(float yaw) {
    return newInstance(getPitch(), yaw, getRoll());
  }

  public AngleN setRoll(float roll) {
    return newInstance(getPitch(), getYaw(), roll);
  }

  public abstract boolean isInDegrees();

  public boolean isInRadians() {
    return !isInDegrees();
  }

  public AngleN add(AngleN ang) {
    return newInstance(
        getPitch() + ang.same(this).getPitch(),
        getYaw() + ang.same(this).getYaw(),
        getRoll() + ang.same(this).getRoll());
  }

  public AngleN add(float p, float y, float r) {
    return add(newInstance(p, y, r));
  }

  public AngleN add(float p, float y) {
    return add(p, y, 0.f);
  }

  public AngleN sub(AngleN ang) {
    return add(ang.scale(-1));
  }

  public AngleN sub(float p, float y, float r) {
    return add(-p, -y, -r);
  }

  public AngleN sub(float p, float y) {
    return sub(p, y, 0.f);
  }

  public AngleN scale(float factor) {
    return newInstance(getPitch() * factor, getYaw() * factor, getRoll() * factor);
  }

  public abstract AngleN normalize();

  public double[] getForwardVector() {
    // x = cos(yaw)cos(pitch)
    // y = sin(pitch)
    // z = sin(yaw)cos(pitch)
    double kps = Math.sin(inRadians().getPitch());
    double kpc = Math.cos(inRadians().getPitch());
    double kys = Math.sin(inRadians().getYaw());
    double kyc = Math.cos(inRadians().getYaw());
    return new double[] {
      kpc * kyc, // x
      kps, // y
      kpc * kys // z
    };
  }

  public float[] toArray() {
    return new float[] {getPitch(), getYaw(), getRoll()};
  }

  public abstract AngleN inRadians();

  public abstract AngleN inDegrees();

  protected AngleN same(AngleN other) {
    return other.isInDegrees() ? inDegrees() : inRadians();
  }

  protected abstract AngleN newInstance(float pitch, float yaw, float roll);

  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof AngleN && AngleHelper.isEqual(this, (AngleN) obj));
  }

  @Override
  public int hashCode() {
    AngleN a = normalize().inDegrees();
    return Objects.hash(a.getPitch(), a.getYaw(), a.getRoll());
  }

  @Override
  public String toString() {
    return String.format(
        "(%.15f, %.15f, %.15f)[%s]",
        getPitch(), getYaw(), getRoll(), isInRadians() ? "rad" : "deg");
  }

  static class Degrees extends AngleN {
    private Radians radians = null;

    private Degrees(float pitch, float yaw, float roll) {
      super(pitch, yaw, roll);
    }

    @Override
    public boolean isInDegrees() {
      return true;
    }

    @Override
    public AngleN normalize() {
      return newInstance(
          AngleHelper.normalizeInDegrees(getPitch()),
          AngleHelper.normalizeInDegrees(getYaw()),
          AngleHelper.normalizeInDegrees(getRoll()));
    }

    @Override
    public AngleN inRadians() {
      return radians == null
          ? radians =
              (Radians)
                  radians(
                      Math.toRadians(getPitch()),
                      Math.toRadians(getYaw()),
                      Math.toRadians(getRoll()))
          : radians;
    }

    @Override
    public AngleN inDegrees() {
      return this;
    }

    @Override
    protected AngleN newInstance(float pitch, float yaw, float roll) {
      return new Degrees(pitch, yaw, roll);
    }
  }

  static class Radians extends AngleN {
    private Degrees degrees = null;

    private Radians(float pitch, float yaw, float roll) {
      super(pitch, yaw, roll);
    }

    @Override
    public boolean isInDegrees() {
      return false;
    }

    @Override
    public AngleN normalize() {
      return newInstance(
          AngleHelper.normalizeInRadians(getPitch()),
          AngleHelper.normalizeInRadians(getYaw()),
          AngleHelper.normalizeInRadians(getRoll()));
    }

    @Override
    public AngleN inRadians() {
      return this;
    }

    @Override
    public AngleN inDegrees() {
      return degrees == null
          ? degrees =
              (Degrees)
                  degrees(
                      Math.toDegrees(getPitch()),
                      Math.toDegrees(getYaw()),
                      Math.toDegrees(getRoll()))
          : degrees;
    }

    @Override
    protected AngleN newInstance(float pitch, float yaw, float roll) {
      return new Radians(pitch, yaw, roll);
    }
  }
}
