package com.matt.forgehax.util.entity;

import static com.matt.forgehax.Helper.*;

import com.matt.forgehax.Globals;
import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.util.math.Angle;
import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Class for dealing with the local player only */
public class LocalPlayerUtils implements Globals {
  /** Gets the players current view angles */
  public static Angle getViewAngles() {
    return PositionRotationManager.getState().getRenderClientViewAngles();
  }

  public static Angle getServerViewAngles() {
    return PositionRotationManager.getState().getRenderServerViewAngles();
  }

  public static Vec3d getVelocity() {
    return new Vec3d(getLocalPlayer().motionX, getLocalPlayer().motionY, getLocalPlayer().motionZ);
  }

  private final Vec3d getVectorForRotation(float pitch, float yaw) {
    float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
    float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
    float f2 = -MathHelper.cos(-pitch * 0.017453292F);
    float f3 = MathHelper.sin(-pitch * 0.017453292F);
    return new Vec3d((double) (f1 * f2), (double) f3, (double) (f * f2));
  }

  public static BlockPlacementInfo getBlockPlacementInfo(final BlockPos pos) {
    final Vec3d eyes = EntityUtils.getEyePos(getLocalPlayer());
    final Vec3d center = new Vec3d(pos).addVector(0.5D, 0.5D, 0.5D);
    final Vec3d normal = getServerViewAngles().getDirectionVector().normalize();
    return Arrays.stream(EnumFacing.values())
        .map(side -> new BlockPlacementInfo(pos.offset(side), side))
        .filter(
            info -> eyes.squareDistanceTo(center) < eyes.squareDistanceTo(info.getCenteredPos()))
        .filter(
            info -> info.getBlockState().getBlock().canCollideCheck(info.getBlockState(), false))
        .filter(
            info ->
                eyes.squareDistanceTo(info.getHitVec())
                    < MC.playerController.getBlockReachDistance()
                        * MC.playerController.getBlockReachDistance())
        .min(
            Comparator.comparingDouble(
                info ->
                    new Vec3d(info.getPos())
                        .subtract(new Vec3d(pos))
                        .normalize()
                        .subtract(normal)
                        .lengthSquared()))
        .orElse(null);
  }

  public static class BlockPlacementInfo {
    private final BlockPos pos;
    private final EnumFacing side;

    public BlockPlacementInfo(BlockPos pos, EnumFacing side) {
      this.pos = pos;
      this.side = side;
    }

    public BlockPos getPos() {
      return pos;
    }

    public EnumFacing getSide() {
      return side;
    }

    public EnumFacing getOppositeSide() {
      return side.getOpposite();
    }

    public Vec3d getHitVec() {
      return getCenteredPos().add(new Vec3d(getOppositeSide().getDirectionVec()).scale(0.5D));
    }

    public Vec3d getCenteredPos() {
      return new Vec3d(getPos()).addVector(0.5D, 0.5D, 0.5D);
    }

    public IBlockState getBlockState() {
      return getWorld().getBlockState(getPos());
    }
  }
}
