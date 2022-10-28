package com.matt.forgehax.util.entity;

import static com.matt.forgehax.Helper.*;

import com.matt.forgehax.Globals;
import com.matt.forgehax.util.math.AngleESP;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.FMLClientHandler;

/** Class for dealing with the local player only */
public class LocalPlayerUtilsESP implements Globals {
  private static boolean projectileTargetAcquired = false;
  private static boolean activeFakeAngleESPs = false;
  private static Entity targetEntity = null;
  private static AngleESP fakeViewAngleESPs = null;

  /**
   * If the client should be sending fake angles to the server Will override any vanilla packets
   * sent to the server and change them as well.
   */
  public static boolean isFakeAngleESPsActive() {
    return activeFakeAngleESPs;
  }

  /** Sets the active fake angles flag */
  public static void setActiveFakeAngleESPs(boolean aiming) {
    LocalPlayerUtilsESP.activeFakeAngleESPs = aiming;
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
    LocalPlayerUtilsESP.projectileTargetAcquired = projectileTargetAcquired;
  }

  /** Sets the players real view angles */
  public static void setViewAngleESPs(AngleESP angles) {
    setViewAngleESPs(angles.getPitch(), angles.getYaw());
  }

  public static void setViewAngleESPs(double p, double y) {
    getLocalPlayer().rotationYaw = (float) y;
    getLocalPlayer().rotationPitch = (float) p;
  }

  /** Gets the players current view angles */
  public static AngleESP getViewAngleESPs() {
    return new AngleESP(getLocalPlayer().rotationPitch, getLocalPlayer().rotationYaw);
  }

  /** Sets the fake angles that will sent to the server */
  public static void setFakeViewAngleESPs(AngleESP fakeViewAngleESPs) {
    LocalPlayerUtilsESP.fakeViewAngleESPs = fakeViewAngleESPs;
  }

  /** Gets the currently set fake angles */
  public static AngleESP getFakeViewAngleESPs() {
    return fakeViewAngleESPs;
  }

  /*
  public static void correctMovement(AngleESP viewAngleESPs) {
      AngleESP mcView = new AngleESP(MathHelper.wrapDegrees(me().rotationPitch), MathHelper.wrapDegrees(me().rotationYaw));
      Vec3d move = new Vec3d(me().moveForward, 0, me().moveStrafing);
      AngleESP angle = VectorUtils.vectorToAngleESP(move.normalize()).add(viewAngleESPs.sub(mcView));
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
    LocalPlayerUtilsESP.targetEntity = targetEntity;
  }

  /** Check if entity instance is our target */
  public static boolean isTargetEntity(Entity entity) {
    return targetEntity != null && targetEntity.equals(entity);
  }

  /** Sends a player rotation packet to the server */
  private static final AngleESP lastAngleESP = new AngleESP(0, 0, 0);

  public static void sendRotatePacket(double pitch, double yaw) {
    if (lastAngleESP.getPitch() != pitch || lastAngleESP.getYaw() != yaw) {
      FMLClientHandler.instance()
          .getClientToServerNetworkManager()
          .sendPacket(new CPacketPlayer.Rotation((float) yaw, (float) pitch, MC.player.onGround));
      lastAngleESP.setPitch(pitch);
      lastAngleESP.setYaw(yaw);
    }
  }

  public static void sendRotatePacket(AngleESP angle) {
    sendRotatePacket(angle.getPitch(), angle.getYaw());
  }
}
