package com.matt.forgehax.util.entity;

import static com.matt.forgehax.Helper.*;

import com.matt.forgehax.Globals;
import com.matt.forgehax.util.math.Angle;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.FMLClientHandler;

/** Class for dealing with the local player only */
public class LocalPlayerUtils implements Globals {
  private static boolean projectileTargetAcquired = false;
  private static boolean activeFakeAngles = false;
  private static Entity targetEntity = null;
  private static Angle fakeViewAngles = null;

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

  /** Sets the players real view angles */
  public static void setViewAngles(Angle angles) {
    setViewAngles(angles.getPitch(), angles.getYaw());
  }

  public static void setViewAngles(double p, double y) {
    getLocalPlayer().rotationYaw = (float) y;
    getLocalPlayer().rotationPitch = (float) p;
  }

  /** Gets the players current view angles */
  public static Angle getViewAngles() {
    return new Angle(getLocalPlayer().rotationPitch, getLocalPlayer().rotationYaw);
  }

  /** Sets the fake angles that will sent to the server */
  public static void setFakeViewAngles(Angle fakeViewAngles) {
    LocalPlayerUtils.fakeViewAngles = fakeViewAngles;
  }

  /** Gets the currently set fake angles */
  public static Angle getFakeViewAngles() {
    return fakeViewAngles;
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

  /** Sends a player rotation packet to the server */
  private static Angle lastAngle = new Angle(0, 0, 0);

  public static void sendRotatePacket(double pitch, double yaw) {
    if (lastAngle.getPitch() != pitch || lastAngle.getYaw() != yaw) {
      FMLClientHandler.instance()
          .getClientToServerNetworkManager()
          .sendPacket(new CPacketPlayer.Rotation((float) yaw, (float) pitch, MC.player.onGround));
      lastAngle.setPitch(pitch);
      lastAngle.setYaw(yaw);
    }
  }

  public static void sendRotatePacket(Angle angle) {
    sendRotatePacket(angle.getPitch(), angle.getYaw());
  }
}
