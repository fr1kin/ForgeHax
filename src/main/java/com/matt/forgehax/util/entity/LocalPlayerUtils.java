package com.matt.forgehax.util.entity;

import static com.matt.forgehax.Helper.*;

import com.google.common.base.Predicates;
import com.matt.forgehax.Globals;
import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.mods.services.SneakService;
import com.matt.forgehax.util.Switch;
import com.matt.forgehax.util.math.Angle;
import java.util.Optional;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;

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
    return getLocalPlayer().getMotion();
  }

  public static boolean isSneaking() {
    return getLocalPlayer().isSneaking();
  }

  public static boolean setSneaking(boolean sneak) {
    boolean old = isSneaking();
    getLocalPlayer().setSneaking(sneak);
    if (getLocalPlayer().movementInput != null) getLocalPlayer().movementInput.sneak = sneak;
    return old;
  }

  public static void setSneakingSuppression(boolean suppress) {
    SneakService.getInstance().setSuppressing(suppress);
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

  public static RayTraceResult getViewTrace() {
    return MC.objectMouseOver;
  }

  public static BlockRayTraceResult getMouseOverBlockTrace() {
    return Optional.ofNullable(MC.objectMouseOver)
        .filter(tr -> tr.getType() == Type.BLOCK)
        .map(tr -> (BlockRayTraceResult)tr)
        .filter(tr -> tr.getPos() != null) // no its not intelliJ
        .orElse(null);
  }

  public static RayTraceResult getViewTrace(
      Entity entity, Vec3d direction, float partialTicks, double reach, double reachAttack) {
    if (entity == null) return null;

    Vec3d eyes = entity.getEyePosition(partialTicks);
    RayTraceResult trace = entity.rayTrace(reach, partialTicks, RayTraceFluidMode.NEVER);

    Vec3d dir = direction.scale(reach);
    Vec3d lookDir = eyes.add(dir);

    double hitDistance = trace == null ? reachAttack : trace.hitVec.distanceTo(eyes);
    Entity hitEntity = null;
    Vec3d hitEntityVec = null;

    for (Entity ent :
        getWorld()
            .getEntitiesInAABBexcluding(
                entity,
                entity.getBoundingBox().expand(dir.x, dir.y, dir.y).grow(1.D),
                EntitySelectors.NOT_SPECTATING.and(e -> e != null && e.canBeCollidedWith()))
      )
    {
      AxisAlignedBB bb = ent.getBoundingBox().grow(ent.getCollisionBorderSize());
      RayTraceResult tr = bb.calculateIntercept(eyes, lookDir);
      if (bb.contains(eyes)) {
        if (hitDistance > 0.D) {
          hitEntity = ent;
          hitEntityVec = tr == null ? eyes : tr.hitVec;
          hitDistance = 0.D;
        }
      } else if (tr != null) {
        double dist = eyes.distanceTo(tr.hitVec);
        if (dist < hitDistance || hitDistance == 0.D) {
          if (entity.getLowestRidingEntity() == ent.getLowestRidingEntity()
              && !ent.canRiderInteract()) {
            if (hitDistance == 0.D) {
              hitEntity = ent;
              hitEntityVec = tr.hitVec;
            }
          } else {
            hitEntity = ent;
            hitEntityVec = tr.hitVec;
            hitDistance = dist;
          }
        }
      }
    }

    if (hitEntity != null && reach > 3.D && eyes.distanceTo(hitEntityVec) > 3.D)
      return new RayTraceResult(Type.MISS, hitEntityVec, Direction.UP, new BlockPos(hitEntityVec));
    else if (hitEntity != null && trace == null && hitDistance < reachAttack)
      return new RayTraceResult(hitEntity, hitEntityVec);
    else return trace;
  }

  public static boolean isInReach(Vec3d start, Vec3d end) {
    return start.squareDistanceTo(end)
        < getPlayerController().getBlockReachDistance()
            * getPlayerController().getBlockReachDistance();
  }

  private static final Switch FLY_SWITCH = new Switch("PlayerFlying") {
    private static final float DEFAULT_FLY_SPEED = 0.05f;

    @Override
    protected void onEnabled() {
      MC.execute(() -> {
        if(getLocalPlayer() == null || getLocalPlayer().playerAbilities == null)
          return;

        getLocalPlayer().playerAbilities.allowFlying = true;
        getLocalPlayer().playerAbilities.isFlying = true;
      });
    }

    @Override
    protected void onDisabled() {
      MC.execute(() -> {
        if(getLocalPlayer() == null || getLocalPlayer().playerAbilities == null)
          return;

        getLocalPlayer().playerAbilities.allowFlying = false;
        getLocalPlayer().playerAbilities.isFlying = false;
        getLocalPlayer().playerAbilities.setFlySpeed(DEFAULT_FLY_SPEED);
      });
    }
  };

  public static Switch getFlySwitch() {
    return FLY_SWITCH;
  }
}
