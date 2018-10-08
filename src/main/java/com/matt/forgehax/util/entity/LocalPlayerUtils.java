package com.matt.forgehax.util.entity;

import static com.matt.forgehax.Helper.*;

import com.matt.forgehax.Globals;
import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.util.math.AngleN;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/** Class for dealing with the local player only */
public class LocalPlayerUtils implements Globals {
  private static boolean projectileTargetAcquired = false;
  private static boolean activeFakeAngles = false;
  private static Entity targetEntity = null;

  /**
   * If the client should be sending fake angles to the server Will override any vanilla packets
   * sent to the server and change them as well.
   */
  public static boolean isFakeAnglesActive() {
    return activeFakeAngles;
  }

  /** Sets the active fake angles flag */
  public static void setActiveFakeAngles(boolean aiming) {
    LocalPlayerUtils.activeFakeAngles = aiming;
  }

  /**
   * If the fake angles are set, but not active because we are dealing with a projectile weapon.
   * This flag will tell if we have a projectile target and are waiting for the use input to be
   * released by the player.
   */
  public static boolean isProjectileTargetAcquired() {
    return projectileTargetAcquired;
  }

  /** Sets the projectile target acquired flag */
  public static void setProjectileTargetAcquired(boolean projectileTargetAcquired) {
    LocalPlayerUtils.projectileTargetAcquired = projectileTargetAcquired;
  }

  /** Gets the players current view angles */
  public static AngleN getViewAngles() {
    return PositionRotationManager.getState().getRenderClientViewAngles();
  }

  /*
  public static void correctMovement(Angle viewAngles) {
      Angle mcView = new Angle(MathHelper.wrapDegrees(me().rotationPitch), MathHelper.wrapDegrees(me().rotationYaw));
      Vec3d move = new Vec3d(me().moveForward, 0, me().moveStrafing);
      Angle angle = VectorUtils.vectorToAngle(move.normalize()).add(viewAngles.sub(mcView));
      Vec3d forward = angle.forward().scale(move.lengthVector());
      me().moveForward = (float)forward.xCoord;
      me().moveStrafing = (float)forward.zCoord;
  }
  */

  public static Vec3d getVelocity() {
    return new Vec3d(getLocalPlayer().motionX, getLocalPlayer().motionY, getLocalPlayer().motionZ);
  }

  /** Get the aimbot mods current target entity */
  public static Entity getTargetEntity() {
    return targetEntity;
  }

  /** Set the aimbot mods current target entity (can be null to disable) */
  public static void setTargetEntity(@Nullable Entity targetEntity) {
    LocalPlayerUtils.targetEntity = targetEntity;
  }

  /** Check if entity instance is our target */
  public static boolean isTargetEntity(Entity entity) {
    return targetEntity != null && targetEntity.equals(entity);
  }
}
