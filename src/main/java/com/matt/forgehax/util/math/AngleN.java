package com.matt.forgehax.util.math;

import java.util.Arrays;

/**
 * Created on 6/21/2017 by fr1kin
 *
 * <p>TODO: replace Angle.java with this
 */
public class AngleN {
  public static final AngleN ZERO = new AngleN(true, 0.D, 0.D, 0.D);

  public static AngleN radians(double pitch, double yaw, double roll) {
    return new AngleN(true, pitch, yaw, roll);
  }

  public static AngleN radians(float pitch, float yaw, float roll) {
    return radians((double) pitch, (double) yaw, (double) roll);
  }

  public static AngleN radians(int pitch, int yaw, int roll) {
    return radians((double) pitch, (double) yaw, (double) roll);
  }

  public static AngleN radians(double pitch, double yaw) {
    return radians(pitch, yaw, 0.D);
  }

  public static AngleN radians(float pitch, float yaw) {
    return radians(pitch, yaw, 0.f);
  }

  public static AngleN radians(int pitch, int yaw) {
    return radians(pitch, yaw, 0);
  }

  public static AngleN degrees(double pitch, double yaw, double roll) {
    return new AngleN(false, pitch, yaw, roll);
  }

  public static AngleN degrees(float pitch, float yaw, float roll) {
    return degrees((double) pitch, (double) yaw, (double) roll);
  }

  public static AngleN degrees(int pitch, int yaw, int roll) {
    return degrees((double) pitch, (double) yaw, (double) roll);
  }

  public static AngleN degrees(double pitch, double yaw) {
    return degrees(pitch, yaw, 0.D);
  }

  public static AngleN degrees(float pitch, float yaw) {
    return degrees(pitch, yaw, 0.f);
  }

  public static AngleN degrees(int pitch, int yaw) {
    return degrees(pitch, yaw, 0);
  }

  public static AngleN copy(AngleN ang) {
    return new AngleN(ang);
  }

  // if the numbers stored are in radians
  private final boolean radians;

  private final double pitch;
  private final double yaw;
  private final double roll;

  // opposite type (i.e if this is an angle in radians, twin will equal this angle in degrees)
  private AngleN twin = null;
  // normal angle of this
  private AngleN normal = null;

  protected AngleN(boolean radians, double pitch, double yaw, double roll) {
    this.radians = radians;
    this.pitch = pitch;
    this.yaw = yaw;
    this.roll = roll;
  }

  protected AngleN(AngleN ang) {
    this(ang.isRadians(), ang.pitch(), ang.yaw(), ang.roll());
    // don't copy twin and normal fields
  }

  public double pitch() {
    return pitch;
  }

  public double yaw() {
    return yaw;
  }

  public double roll() {
    return roll;
  }

  public boolean isRadians() {
    return radians;
  }

  public boolean isDegrees() {
    return !radians;
  }

  public AngleN toRadians() {
    if (isRadians()) return this;
    else {
      if (twin == null) {
        this.twin = radians(Math.toRadians(pitch()), Math.toRadians(yaw()), Math.toRadians(roll()));
        // sets the newly created objects opposite to this
        this.twin.twin = this;
      }
      return twin;
    }
  }

  public AngleN toDegrees() {
    if (isDegrees()) return this;
    else {
      if (twin == null) {
        this.twin = degrees(Math.toDegrees(pitch()), Math.toDegrees(yaw()), Math.toDegrees(roll()));
        // sets the newly created objects opposite to this
        this.twin.twin = this;
      }
      return twin;
    }
  }

  public AngleN add(double p, double y, double r) {
    return create(pitch() + p, yaw() + y, roll() + r);
  }

  public AngleN add(double p, double y) {
    return add(p, y, 0.D);
  }

  public AngleN add(AngleN ang) {
    // make angle in match this angles, result will always be in the form of the calling angle
    if (isRadians() != ang.isRadians()) ang = isRadians() ? ang.toRadians() : ang.toDegrees();
    return add(ang.pitch(), ang.yaw(), ang.roll());
  }

  public AngleN sub(double p, double y, double r) {
    return add(-p, -y, -r);
  }

  public AngleN sub(double p, double y) {
    return sub(p, y, 0.D);
  }

  public AngleN sub(AngleN ang) {
    // make angle in match this angles, result will always be in the form of the calling angle
    if (isRadians() != ang.isRadians()) ang = isRadians() ? ang.toRadians() : ang.toDegrees();
    return sub(ang.pitch(), ang.yaw(), ang.roll());
  }

  public AngleN scale(double factor) {
    return create(pitch() * factor, yaw() * factor, roll() * factor);
  }

  public AngleN normalize() {
    if (this.normal == null) {
      double np, ny, nr;
      if (isRadians()) {
        np = AngleHelper.normalizeInRadians(pitch());
        ny = AngleHelper.normalizeInRadians(yaw());
        nr = AngleHelper.normalizeInRadians(roll());
      } else {
        np = AngleHelper.normalizeInDegrees(pitch());
        ny = AngleHelper.normalizeInDegrees(yaw());
        nr = AngleHelper.normalizeInDegrees(roll());
      }
      if (AngleHelper.isAngleEqual(pitch(), np)
          && AngleHelper.isAngleEqual(yaw(), ny)
          && AngleHelper.isAngleEqual(roll(), nr)) {
        // this angle is already normalized, no need to create new angle
        this.normal = this;
      } else {
        // create new angle for the normalized angle
        AngleN norm = create(np, ny, nr);
        // set the normal angles normal field
        norm.normal = norm;
        this.normal = norm;
      }
    }
    return this.normal;
  }

  public double[] forward() {
    // x = cos(yaw)cos(pitch)
    // y = sin(pitch)
    // z = sin(yaw)cos(pitch)
    double kps = Math.sin(toRadians().pitch());
    double kpc = Math.cos(toRadians().pitch());
    double kys = Math.sin(toRadians().yaw());
    double kyc = Math.cos(toRadians().yaw());
    return new double[] {
      kpc * kyc, // x
      kps, // y
      kpc * kys // z
    };
  }

  /*
  // Direction vector from roll

  // https://en.wikipedia.org/wiki/Euler_angles
  // https://en.wikipedia.org/wiki/Rotation_matrix#In_three_dimensions

  public Vec3d forward() {
      // x = cos(r)cos(y)
      // y = -sin(y)
      // z = sin(r)cos(y)
      AngleN ar = toRadians();
      double kys = Math.sin(ar.yaw());
      double kyc = Math.cos(ar.yaw());
      double krs = Math.sin(ar.roll());
      double krc = Math.cos(ar.roll());
      return new Vec3d(
              krc * kyc,
              -kys,
              krs * kyc
      );
  }

  // Math for these two methods might be wrong
  // Also the y and z need to have their places changed
  public Vec3d right() {
      // x = sin(r)sin(p) + cos(r)sin(y)cos(p)
      // y = -cos(r)sin(p) + sin(r)sin(y)cos(p)
      // z = cos(y)cos(p)
      AngleN ar = toRadians();
      double kps = Math.sin(ar.pitch());
      double kpc = Math.cos(ar.pitch());
      double kys = Math.sin(ar.yaw());
      double kyc = Math.cos(ar.yaw());
      double krs = Math.sin(ar.roll());
      double krc = Math.cos(ar.roll());
      return new Vec3d(
              (krs * kps) + krc * kys * kpc,
              (-krc * kps) + krs * kys * kpc,
              kyc * kpc
      );
  }

  public Vec3d up() {
      // x = -sin(r)cos(p) + cos(r)sin(y)sin(p)
      // y = cos(r)cos(p) + sin(r)sin(y)sin(p)
      // z = cos(y)sin(p)
      AngleN ar = toRadians();
      double kps = Math.sin(ar.pitch());
      double kpc = Math.cos(ar.pitch());
      double kys = Math.sin(ar.yaw());
      double kyc = Math.cos(ar.yaw());
      double krs = Math.sin(ar.roll());
      double krc = Math.cos(ar.roll());
      return new Vec3d(
              (-krs * kpc) + (krc * kys * kps),
              (krc * kpc) + (krs * kys * kps),
              kyc * kps
      );
  }
  //*/

  public double[] toArray() {
    return new double[] {pitch(), yaw(), roll()};
  }

  protected AngleN create(double p, double y, double r) {
    return new AngleN(isRadians(), p, y, r);
  }

  protected AngleN create(double p, double y) {
    return create(p, y, 0.D);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof AngleN) {
      // normalize both vectors
      AngleN self = this.normalize();
      AngleN ang = ((AngleN) obj).normalize();
      if (self.isRadians() != ang.isRadians())
        ang = self.isRadians() ? ang.toRadians() : ang.toDegrees();
      return AngleHelper.isAngleEqual(self.pitch(), ang.pitch())
          && AngleHelper.isAngleEqual(self.yaw(), ang.yaw())
          && AngleHelper.isAngleEqual(self.roll(), ang.roll());
    } else return false;
  }

  @Override
  public int hashCode() {
    AngleN a = normalize().toRadians();
    return Arrays.hashCode(
        new double[] {
          AngleHelper.roundAngle(a.pitch()),
          AngleHelper.roundAngle(a.yaw()),
          AngleHelper.roundAngle(a.roll())
        });
  }

  @Override
  public String toString() {
    return String.format(
        "(%.15f, %.15f, %.15f)[%s]", pitch(), yaw(), roll(), isRadians() ? "rad" : "deg");
  }
}
