package dev.fiki.forgehax.main.util.math;

import dev.fiki.forgehax.main.Common;
import lombok.Getter;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.math.vector.Vector4f;

import static dev.fiki.forgehax.main.Common.*;

public class VectorUtils implements Common {
  // Credits to Gregor and P47R1CK for the 3D vector transformation code

//    if(dot > 0) {
//      return new Plane(0.D, 0.D, false);
//    }
//
//    // vertical fov
//    double fov = FastReflection.Methods.GameRenderer_getFOVModifier.invoke(getGameRenderer(),
//        getGameRenderer().getActiveRenderInfo(), MC.getRenderPartialTicks(), true);
//
//    double frameWidth = getMainWindow().getFramebufferWidth();
//    double frameHeight = getMainWindow().getFramebufferHeight();
//
//    // aspect ratio (w/h)
//    double aspectRatio = frameWidth / frameHeight;
//
//    // horizontal fov
//    //double fovHorizontal = Math.atan(aspectRatio * Math.tan(Math.toRadians(fov) / 2.f));
//    double fovVertical = Math.toRadians(getGameSettings().fov - (fov - 70));
//    double fovHorizontal = (1.d / Math.tan(fovVertical / 2.d));
//
//    double d = (screenHeight) / fovHorizontal;
//
//    double scalar = d / dot;
//    Vector3d projection = dir.scale(scalar);
//
//    double pointX = 0.5f * screenWidth + right.dotProduct(projection);
//    double pointY = 0.5f * screenHeight - up.dotProduct(projection);

  @Getter
  private static Matrix4f projectionMatrix = new Matrix4f();
  @Getter
  private static Matrix4f viewMatrix = new Matrix4f();
  @Getter
  private static Matrix4f projectionViewMatrix = new Matrix4f();

  public static void setProjectionViewMatrix(Matrix4f projection, Matrix4f view) {
    projectionMatrix = projection.copy();
    viewMatrix = view.copy();

    projectionViewMatrix = projectionMatrix.copy();
    projectionViewMatrix.mul(viewMatrix);
  }

  /**
   * Convert 3D coord into 2D coordinate projected onto the screen
   */
  public static Plane toScreen(Vector3d vector) {
    final double screenWidth = getScreenWidth();
    final double screenHeight = getScreenHeight();

    Vector3d camera = getGameRenderer().getActiveRenderInfo().getProjectedView();
    Vector3d dir = camera.subtract(vector);

    Vector4f pos = new Vector4f((float) dir.getX(), (float) dir.getY(), (float) dir.getZ(), 1.f);

    pos.transform(projectionViewMatrix);
    double w = pos.getW();
    pos.perspectiveDivide();

    double halfWidth = screenWidth / 2.d;
    double halfHeight = screenHeight / 2.d;

    double pointX = (halfWidth * pos.getX()) + (pos.getX() + halfWidth);
    double pointY = -(halfHeight * pos.getY()) + (pos.getY() + halfHeight);

    return new Plane(pointX, pointY, w < 0.1d);
  }

  public static Plane toScreen(double x, double y, double z) {
    return toScreen(new Vector3d(x, y, z));
  }

  @Deprecated
  public static ScreenPos _toScreen(double x, double y, double z) {
    Plane plane = toScreen(x, y, z);
    return new ScreenPos(plane.getX(), plane.getY(), plane.isVisible());
  }

  @Deprecated
  public static ScreenPos _toScreen(Vector3d Vector3d) {
    return _toScreen(Vector3d.x, Vector3d.y, Vector3d.z);
  }

  /**
   * Convert a vector to a angle
   */
  @Deprecated
  public static Object vectorAngle(Vector3d Vector3d) {
    return null;
  }

  public static Vector3d multiplyBy(Vector3d vec1, Vector3d vec2) {
    return new Vector3d(vec1.x * vec2.x, vec1.y * vec2.y, vec1.z * vec2.z);
  }

  public static Vector3d copy(Vector3d toCopy) {
    return new Vector3d(toCopy.x, toCopy.y, toCopy.z);
  }

  public static double getCrosshairDistance(Vector3d eyes, Vector3d directionVec, Vector3d pos) {
    return pos.subtract(eyes).normalize().subtract(directionVec).lengthSquared();
  }

  public static Vector3d toFPIVector(Vector3i vec) {
    return new Vector3d(vec.getX(), vec.getY(), vec.getZ());
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
