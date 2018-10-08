package com.matt.forgehax.util.math;

import net.minecraft.util.math.Vec3d;

/** Created on 6/21/2017 by fr1kin */
public class AngleHelper {
  public static final long DEFAULT_N = 1000000000L; // 9 decimal places
  public static final double DEFAULT_EPSILON = (1.D / (double) DEFAULT_N);

  public static final double TWO_PI = Math.PI * 2.D; // 180 degrees
  public static final double HALF_PI = Math.PI / 2.D; // 90 degrees
  public static final double QUARTER_PI = Math.PI / 4.D; // 45 degrees

  public static double roundAngle(double a, long n) {
    return Math.round(a * n) / (double) n;
  }

  public static double roundAngle(double a) {
    return roundAngle(a, DEFAULT_N);
  }

  public static boolean isAngleEqual(double a1, double a2, double epsilon) {
    return Double.compare(a1, a2) == 0 || Math.abs(a1 - a2) < epsilon;
  }

  public static boolean isAngleEqual(double a1, double a2) {
    return isAngleEqual(a1, a2, 1E-4);
  }

  public static boolean isEqual(AngleN ang1, AngleN ang2) {
    AngleN a1 = ang1.normalize();
    AngleN a2 = ang2.same(a1).normalize();
    return isAngleEqual(a1.getPitch(), a2.getPitch())
        && isAngleEqual(a1.getYaw(), a2.getYaw())
        && isAngleEqual(a1.getRoll(), a2.getRoll());
  }

  public static double normalizeInRadians(double ang) {
    while (ang > Math.PI) ang -= 2 * Math.PI;
    while (ang < -Math.PI) ang += 2 * Math.PI;
    return ang;
  }

  public static float normalizeInRadians(float ang) {
    while (ang > Math.PI) ang -= 2 * Math.PI;
    while (ang < -Math.PI) ang += 2 * Math.PI;
    return ang;
  }

  public static double normalizeInDegrees(double ang) {
    while (ang >= 180.D) ang -= 360.D;
    while (ang < -180.D) ang += 360.D;
    return ang;
  }

  public static float normalizeInDegrees(float ang) {
    while (ang >= 180.f) ang -= 360.f;
    while (ang < -180.f) ang += 360.f;
    return ang;
  }

  public static AngleN getAngleFacingInRadians(Vec3d vector) {
    double pitch, yaw;
    if (vector.x == 0 && vector.z == 0) {
      yaw = 0.D;
      pitch = HALF_PI;
    } else {
      yaw = Math.atan2(vector.z, vector.x) - HALF_PI;
      double mag = Math.sqrt(vector.x * vector.x + vector.z * vector.z);
      pitch = -Math.atan2(vector.y, mag);
    }
    return AngleN.radians((float) pitch, (float) yaw);
  }

  public static AngleN getAngleFacingInDegrees(Vec3d vector) {
    return getAngleFacingInRadians(vector).inDegrees();
  }
}
