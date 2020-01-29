package com.matt.forgehax.util.math;

import com.matt.forgehax.Globals;
import com.matt.forgehax.asm.reflection.FastReflection;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import static com.matt.forgehax.Globals.*;

public class VectorUtils implements Globals {
  // Credits to Gregor and P47R1CK for the 3D vector transformation code
  
  static Matrix4d modelMatrix = new Matrix4d();
  static Matrix4d projectionMatrix = new Matrix4d();
  
  private static void VecTransformCoordinate(Vector4d vec, Matrix4d matrix) {
    double x = vec.x;
    double y = vec.y;
    double z = vec.z;
    vec.x = (x * matrix.m00) + (y * matrix.m10) + (z * matrix.m20) + matrix.m30;
    vec.y = (x * matrix.m01) + (y * matrix.m11) + (z * matrix.m21) + matrix.m31;
    vec.z = (x * matrix.m02) + (y * matrix.m12) + (z * matrix.m22) + matrix.m32;
    vec.w = (x * matrix.m03) + (y * matrix.m13) + (z * matrix.m23) + matrix.m33;
  }
  
  /**
   * Convert 3D coord into 2D coordinate projected onto the screen
   */
  public static Plane toScreen(double x, double y, double z) {
//    Entity view = MC.getRenderViewEntity();
//
//    if (view == null) {
//      return new Plane(0.D, 0.D, false);
//    }
//
//    Vec3d camPos = getGameRenderer().getActiveRenderInfo().getProjectedView();
//    Vec3d eyePos = ActiveRenderInfo.projectViewFromEntity(view, MC.getRenderPartialTicks());
//
//    double vecX = (camPos.x + eyePos.x) - x;
//    double vecY = (camPos.y + eyePos.y) - y;
//    double vecZ = (camPos.z + eyePos.z) - z;
//
//    Vector4d pos = new Vector4d(vecX, vecY, vecZ, 1.f);
//
//    modelMatrix.load(
//        FastReflection.Fields.ActiveRenderInfo_MODELVIEW.getStatic().asReadOnlyBuffer());
//    projectionMatrix.load(
//        FastReflection.Fields.ActiveRenderInfo_PROJECTION.getStatic().asReadOnlyBuffer());
//
//    VecTransformCoordinate(pos, modelMatrix);
//    VecTransformCoordinate(pos, projectionMatrix);
//
//    if (pos.w > 0.f) {
//      pos.x *= -100000;
//      pos.y *= -100000;
//    } else {
//      double invert = 1.f / pos.w;
//      pos.x *= invert;
//      pos.y *= invert;
//    }
//
//    double halfWidth = (double) getScreenWidth()/ 2.f;
//    double halfHeight = (double) getScreenHeight() / 2.f;
//
//    pos.x = halfWidth + (0.5f * pos.x * getScreenWidth() + 0.5f);
//    pos.y = halfHeight - (0.5f * pos.y * getScreenHeight() + 0.5f);
//
//    boolean bVisible = true;
//
//    if (pos.x < 0 || pos.y < 0 || pos.x > getScreenWidth() || pos.y > getScreenHeight()) {
//      bVisible = false;
//    }
//
//    return new Plane(pos.x, pos.y, bVisible);
    throw new UnsupportedOperationException();
    // TODO: 1.15 figure out how to get modelview and projection
  }
  
  public static Plane toScreen(Vec3d vec) {
    return toScreen(vec.x, vec.y, vec.z);
  }
  
  @Deprecated
  public static ScreenPos _toScreen(double x, double y, double z) {
    Plane plane = toScreen(x, y, z);
    return new ScreenPos(plane.getX(), plane.getY(), plane.isVisible());
  }
  
  @Deprecated
  public static ScreenPos _toScreen(Vec3d vec3d) {
    return _toScreen(vec3d.x, vec3d.y, vec3d.z);
  }
  
  /**
   * Convert a vector to a angle
   */
  @Deprecated
  public static Object vectorAngle(Vec3d vec3d) {
    return null;
  }
  
  public static Vec3d multiplyBy(Vec3d vec1, Vec3d vec2) {
    return new Vec3d(vec1.x * vec2.x, vec1.y * vec2.y, vec1.z * vec2.z);
  }
  
  public static Vec3d copy(Vec3d toCopy) {
    return new Vec3d(toCopy.x, toCopy.y, toCopy.z);
  }
  
  public static double getCrosshairDistance(Vec3d eyes, Vec3d directionVec, Vec3d pos) {
    return pos.subtract(eyes).normalize().subtract(directionVec).lengthSquared();
  }
  
  @Deprecated
  public static class ScreenPos {
    
    public final int x;
    public final int y;
    public final boolean isVisible;
    
    public final double xD;
    public final double yD;
    
    public ScreenPos(double x, double y, boolean isVisible) {
      this.x = (int) x;
      this.y = (int) y;
      this.xD = x;
      this.yD = y;
      this.isVisible = isVisible;
    }
  }
}
