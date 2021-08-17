package dev.fiki.forgehax.api.math;

import dev.fiki.forgehax.main.Common;
import lombok.Getter;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.math.vector.Vector4f;

import static dev.fiki.forgehax.main.Common.*;

public class VectorUtil implements Common {
  // Credits to Gregor and P47R1CK for the 3D vector transformation code
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
    projectionViewMatrix.multiply(viewMatrix);
//    projectionViewMatrix.invert();
  }

  /**
   * Convert 3D coord into 2D coordinate projected onto the screen
   */
  public static ScreenPos toScreen(double x, double y, double z) {
    // 0.05 = near plane, which i found in GameRenderer::getProjectionMatrix
    final float NEAR_PLANE = 0.05f;

    final double screenWidth = getScreenWidth();
    final double screenHeight = getScreenHeight();

    Vector3d camera = getGameRenderer().getMainCamera().getPosition();
    Vector3d dir = camera.subtract(x, y, z);

    Vector4f pos = new Vector4f((float) dir.x(), (float) dir.y(), (float) dir.z(), 1.f);
    pos.transform(projectionViewMatrix);

    float w = pos.w();
    if (w < NEAR_PLANE && w != 0) {
      pos.perspectiveDivide();
    } else {
      // epic trick to get off screen coordinates to be in the correct orientation
      // then we scale the coordinate because we want it to be off screen
      float scale = (float) Math.max(screenWidth, screenHeight);
      pos.setX(pos.x() * -1 * scale);
      pos.setY(pos.y() * -1 * scale);
    }

    double hw = screenWidth / 2.d;
    double hh = screenHeight / 2.d;
    double pointX = (hw * pos.x()) + (pos.x() + hw);
    double pointY = -(hh * pos.y()) + (pos.y() + hh);

    return new ScreenPos(pointX, pointY,
        pointX >= 0
            && pointX < screenWidth
            && pointY >= 0
            && pointY < screenHeight);
  }

  public static ScreenPos toScreen(Vector3d vec) {
    return toScreen(vec.x(), vec.y(), vec.z());
  }

  public static ScreenPos toScreen(Vector3i vec) {
    return toScreen(vec.getX(), vec.getY(), vec.getZ());
  }

  public static Vector3d multiplyBy(Vector3d vec1, Vector3d vec2) {
    return new Vector3d(vec1.x * vec2.x, vec1.y * vec2.y, vec1.z * vec2.z);
  }

  public static Vector3d copy(Vector3d toCopy) {
    return new Vector3d(toCopy.x, toCopy.y, toCopy.z);
  }

  public static double getCrosshairDistance(Vector3d eyes, Vector3d directionVec, Vector3d pos) {
    return pos.subtract(eyes).normalize().subtract(directionVec).lengthSqr();
  }

  public static Vector3d toFPIVector(Vector3i vec) {
    return new Vector3d(vec.getX(), vec.getY(), vec.getZ());
  }
}
