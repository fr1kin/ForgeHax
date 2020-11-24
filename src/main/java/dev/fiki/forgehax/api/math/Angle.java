package dev.fiki.forgehax.api.math;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Objects;

/**
 * Created on 6/21/2017 by fr1kin
 */
public abstract class Angle {
  
  public static final Angle ZERO = degrees(0.f, 0.f, 0.f);
  
  public static Angle radians(float pitch, float yaw, float roll) {
    return new Radians(pitch, yaw, roll);
  }
  
  public static Angle radians(float pitch, float yaw) {
    return radians(pitch, yaw, 0.f);
  }
  
  public static Angle radians(double pitch, double yaw, double roll) {
    return radians(
        (float) AngleUtil.roundAngle(pitch),
        (float) AngleUtil.roundAngle(yaw),
        (float) AngleUtil.roundAngle(roll));
  }
  
  public static Angle radians(double pitch, double yaw) {
    return radians(pitch, yaw, 0.D);
  }
  
  public static Angle degrees(float pitch, float yaw, float roll) {
    return new Degrees(pitch, yaw, roll);
  }
  
  public static Angle degrees(float pitch, float yaw) {
    return degrees(pitch, yaw, 0.f);
  }
  
  public static Angle degrees(double pitch, double yaw, double roll) {
    return degrees(
        (float) AngleUtil.roundAngle(pitch),
        (float) AngleUtil.roundAngle(yaw),
        (float) AngleUtil.roundAngle(roll));
  }
  
  public static Angle degrees(double pitch, double yaw) {
    return degrees(pitch, yaw, 0.D);
  }
  
  public static Angle copy(Angle ang) {
    return ang.newInstance(ang.getPitch(), ang.getYaw(), ang.getRoll());
  }
  
  private final float pitch;
  private final float yaw;
  private final float roll;
  
  private Angle(float pitch, float yaw, float roll) {
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
  
  public Angle setPitch(float pitch) {
    return newInstance(pitch, getYaw(), getRoll());
  }
  
  public Angle setYaw(float yaw) {
    return newInstance(getPitch(), yaw, getRoll());
  }
  
  public Angle setRoll(float roll) {
    return newInstance(getPitch(), getYaw(), roll);
  }
  
  public abstract boolean isInDegrees();
  
  public boolean isInRadians() {
    return !isInDegrees();
  }
  
  public Angle add(Angle ang) {
    return newInstance(
        getPitch() + ang.same(this).getPitch(),
        getYaw() + ang.same(this).getYaw(),
        getRoll() + ang.same(this).getRoll());
  }
  
  public Angle add(float p, float y, float r) {
    return add(newInstance(p, y, r));
  }
  
  public Angle add(float p, float y) {
    return add(p, y, 0.f);
  }
  
  public Angle sub(Angle ang) {
    return add(ang.scale(-1));
  }
  
  public Angle sub(float p, float y, float r) {
    return add(-p, -y, -r);
  }
  
  public Angle sub(float p, float y) {
    return sub(p, y, 0.f);
  }
  
  public Angle scale(float factor) {
    return newInstance(getPitch() * factor, getYaw() * factor, getRoll() * factor);
  }
  
  public abstract Angle normalize();
  
  public double[] getForwardVector() {
    // x = cos(yaw)cos(pitch)
    // y = sin(pitch)
    // z = sin(yaw)cos(pitch)
    double kps = Math.sin(inRadians().getPitch());
    double kpc = Math.cos(inRadians().getPitch());
    double kys = Math.sin(inRadians().getYaw());
    double kyc = Math.cos(inRadians().getYaw());
    return new double[]{
        kpc * kyc, // x
        kps, // y
        kpc * kys // z
    };
  }
  
  public Vector3d getDirectionVector() {
    float cy = MathHelper.cos(-inDegrees().getYaw() * 0.017453292F - (float) Math.PI);
    float sy = MathHelper.sin(-inDegrees().getYaw() * 0.017453292F - (float) Math.PI);
    float cp = -MathHelper.cos(-inDegrees().getPitch() * 0.017453292F);
    float sp = MathHelper.sin(-inDegrees().getPitch() * 0.017453292F);
    return new Vector3d(sy * cp, sp, cy * cp);
  }
  
  public float[] toArray() {
    return new float[]{getPitch(), getYaw(), getRoll()};
  }
  
  public abstract Angle inRadians();
  
  public abstract Angle inDegrees();
  
  protected Angle same(Angle other) {
    return other.isInDegrees() ? inDegrees() : inRadians();
  }
  
  protected abstract Angle newInstance(float pitch, float yaw, float roll);
  
  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof Angle && AngleUtil.isEqual(this, (Angle) obj));
  }
  
  @Override
  public int hashCode() {
    Angle a = normalize().inDegrees();
    return Objects.hash(a.getPitch(), a.getYaw(), a.getRoll());
  }
  
  @Override
  public String toString() {
    return String.format(
        "(%.15f, %.15f, %.15f)[%s]",
        getPitch(), getYaw(), getRoll(), isInRadians() ? "rad" : "deg");
  }
  
  static class Degrees extends Angle {
    
    private Radians radians = null;
    
    private Degrees(float pitch, float yaw, float roll) {
      super(pitch, yaw, roll);
    }
    
    @Override
    public boolean isInDegrees() {
      return true;
    }
    
    @Override
    public Angle normalize() {
      return newInstance(
          AngleUtil.normalizeInDegrees(getPitch()),
          AngleUtil.normalizeInDegrees(getYaw()),
          AngleUtil.normalizeInDegrees(getRoll()));
    }
    
    @Override
    public Angle inRadians() {
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
    public Angle inDegrees() {
      return this;
    }
    
    @Override
    protected Angle newInstance(float pitch, float yaw, float roll) {
      return new Degrees(pitch, yaw, roll);
    }
  }
  
  static class Radians extends Angle {
    
    private Degrees degrees = null;
    
    private Radians(float pitch, float yaw, float roll) {
      super(pitch, yaw, roll);
    }
    
    @Override
    public boolean isInDegrees() {
      return false;
    }
    
    @Override
    public Angle normalize() {
      return newInstance(
          AngleUtil.normalizeInRadians(getPitch()),
          AngleUtil.normalizeInRadians(getYaw()),
          AngleUtil.normalizeInRadians(getRoll()));
    }
    
    @Override
    public Angle inRadians() {
      return this;
    }
    
    @Override
    public Angle inDegrees() {
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
    protected Angle newInstance(float pitch, float yaw, float roll) {
      return new Radians(pitch, yaw, roll);
    }
  }
}
