package com.matt.forgehax.util.entity;

import static com.matt.forgehax.Helper.*;

import com.matt.forgehax.Globals;
import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.util.math.Angle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
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

  public static RayTraceResult getMouseOverBlockTrace() {
    return Optional.ofNullable(MC.objectMouseOver)
        .filter(tr -> tr.getBlockPos() != null) // no its not intelliJ
        .filter(
            tr ->
                Type.BLOCK.equals(tr.typeOfHit)
                    || !Material.AIR.equals(
                        getWorld().getBlockState(tr.getBlockPos()).getMaterial()))
        .orElse(null);
  }

  private static RayTraceResult trace(Vec3d start, Vec3d end) {
    return getWorld().rayTraceBlocks(start, end, false, true, false);
  }

  private static BlockPlacementInfo getBlockPlacementInfo(
      final BlockPos pos, Predicate<BlockPlacementInfo> filter) {
    final Vec3d eyes = EntityUtils.getEyePos(getLocalPlayer());
    final Vec3d normal = getServerViewAngles().getDirectionVector().normalize();
    return Arrays.stream(EnumFacing.values())
        .map(side -> new BlockPlacementInfo(pos.offset(side), side))
        .filter(
            info -> info.getBlockState().getBlock().canCollideCheck(info.getBlockState(), false))
        .filter(
            info ->
                eyes.squareDistanceTo(info.getHitVec())
                    < MC.playerController.getBlockReachDistance()
                        * MC.playerController.getBlockReachDistance())
        .filter(filter)
        .min(
            Comparator.comparingDouble(
                info ->
                    new Vec3d(info.getPos())
                        .subtract(eyes)
                        .normalize()
                        .subtract(normal)
                        .lengthSquared()))
        .orElse(null);
  }

  public static BlockPlacementInfo getBlockAroundPlacementInfo(final BlockPos pos) {
    final Vec3d eyes = EntityUtils.getEyePos(getLocalPlayer());
    return getBlockPlacementInfo(
        pos,
        info -> {
          RayTraceResult tr = trace(eyes, new Vec3d(info.getPos()).addVector(0.5D, 0.5D, 0.5D));
          return tr != null
              && info.getOppositeSide().equals(tr.sideHit)
              && info.getPos().equals(tr.getBlockPos());
        });
  }

  public static BlockPlacementInfo getBlockUnderPlacementInfo(final BlockPos pos) {
    final Vec3d eyes = EntityUtils.getEyePos(getLocalPlayer());
    final Vec3d center = new Vec3d(pos).addVector(0.5D, 0.5D, 0.5D);
    return getBlockPlacementInfo(
        pos, info -> eyes.squareDistanceTo(center) < eyes.squareDistanceTo(info.getCenteredPos()));
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
