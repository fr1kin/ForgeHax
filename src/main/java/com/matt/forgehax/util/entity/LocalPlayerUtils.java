package com.matt.forgehax.util.entity;

import static com.matt.forgehax.Helper.*;

import com.matt.forgehax.Globals;
import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.util.math.Angle;
import net.minecraft.util.math.Vec3d;

/** Class for dealing with the local player only */
public class LocalPlayerUtils implements Globals {
  /** Gets the players current view angles */
  public static Angle getViewAngles() {
    return PositionRotationManager.getState().getRenderClientViewAngles();
  }

  public static Vec3d getVelocity() {
    return new Vec3d(getLocalPlayer().motionX, getLocalPlayer().motionY, getLocalPlayer().motionZ);
  }
}
