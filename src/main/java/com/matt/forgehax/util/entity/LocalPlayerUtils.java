package com.matt.forgehax.util.entity;

import static com.matt.forgehax.Helper.*;

import com.matt.forgehax.Globals;
import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.util.math.Angle;
import java.util.Optional;
import net.minecraft.block.material.Material;
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

  public static boolean isSneaking() {
    return getLocalPlayer().movementInput != null && getLocalPlayer().movementInput.sneak;
  }

  public static void setSneaking(boolean sneak) {
    if (getLocalPlayer().movementInput != null) getLocalPlayer().movementInput.sneak = sneak;
  }

  public static Vec3d getEyePos() {
    return EntityUtils.getEyePos(getLocalPlayer());
  }

  public static Vec3d getDirectionVector() {
    return getViewAngles().getDirectionVector().normalize();
  }

  public static Vec3d getServerDirectionVector() {
    return getServerViewAngles().getDirectionVector().normalize();
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

  public static boolean isInReach(Vec3d start, Vec3d end) {
    return start.squareDistanceTo(end)
        < getPlayerController().getBlockReachDistance()
            * getPlayerController().getBlockReachDistance();
  }
}
