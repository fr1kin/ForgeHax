package dev.fiki.forgehax.api.extension;

import dev.fiki.forgehax.api.math.Angle;
import dev.fiki.forgehax.api.math.ScreenPos;
import dev.fiki.forgehax.api.math.VectorUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import static dev.fiki.forgehax.api.math.AngleUtil.HALF_PI;

public class VectorEx {
//  public static Vector3d add(Vector3i vec0, Vector3d vec1) {
//    return new Vector3d(vec0.getX() + vec1.getX(), vec0.y() + vec1.getY(), vec0.getZ() + vec1.getZ());
//  }

  public static Vector3d subtract(Vector3i vec0, Vector3d vec1) {
    return new Vector3d(vec0.getX() - vec1.x(), vec0.getY() - vec1.y(), vec0.getZ() - vec1.z());
  }

  public static ScreenPos toScreen(Vector3d vec) {
    return VectorUtil.toScreen(vec.x(), vec.y(), vec.z());
  }

  public static ScreenPos toScreen(Vector3i vec) {
    return VectorUtil.toScreen(vec.getX(), vec.getY(), vec.getZ());
  }

  public static Vector3d copy(Vector3d toCopy) {
    return new Vector3d(toCopy.x, toCopy.y, toCopy.z);
  }

  public static Vector3d toFpiVector(Vector3i vec) {
    return new Vector3d(vec.getX(), vec.getY(), vec.getZ());
  }

  public static Vector3i toIntVector(Vector3d vec) {
    return new Vector3i(vec.x(), vec.y(), vec.z());
  }

  public static Vector3d getMaxs(AxisAlignedBB bb) {
    return new Vector3d(bb.maxX, bb.maxY, bb.maxZ);
  }

  public static Vector3d getMins(AxisAlignedBB bb) {
    return new Vector3d(bb.minX, bb.minY, bb.minZ);
  }

  public static Angle getAngleFacingInRadians(Vector3d vector) {
    double pitch, yaw;
    if (vector.x == 0 && vector.z == 0) {
      yaw = 0.D;
      pitch = HALF_PI;
    } else {
      yaw = Math.atan2(vector.z, vector.x) - HALF_PI;
      pitch = -Math.atan2(vector.y, Math.sqrt(vector.x * vector.x + vector.z * vector.z));
    }
    return Angle.radians((float) pitch, (float) yaw);
  }

  public static Angle getAngleFacingInDegrees(Vector3d vector) {
    return getAngleFacingInRadians(vector).inDegrees();
  }
}
