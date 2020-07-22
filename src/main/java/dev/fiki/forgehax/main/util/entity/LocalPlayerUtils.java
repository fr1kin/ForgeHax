package dev.fiki.forgehax.main.util.entity;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.mods.managers.PositionRotationManager;
import dev.fiki.forgehax.main.mods.services.SneakService;
import dev.fiki.forgehax.main.util.Switch;
import dev.fiki.forgehax.main.util.math.Angle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Objects;

import static dev.fiki.forgehax.main.Common.*;

/**
 * Class for dealing with the local player only
 */
public class LocalPlayerUtils implements Common {
  
  /**
   * Gets the players current view angles
   */
  public static Angle getViewAngles() {
    return Angle.degrees(getLocalPlayer().rotationPitch, getLocalPlayer().rotationYaw);
    //return PositionRotationManager.getState().getRenderClientViewAngles();
  }
  
  public static Angle getServerViewAngles() {
    return PositionRotationManager.getState().getRenderServerViewAngles();
  }
  
  public static Vector3d getVelocity() {
    return getLocalPlayer().getMotion();
  }

  public static void setViewAngles(float pitch, float yaw) {
    getLocalPlayer().rotationPitch = pitch;
    getLocalPlayer().rotationYaw = yaw;
  }
  
  public static boolean isSneaking() {
    return getLocalPlayer().isCrouching();
  }
  
  public static boolean setSneaking(boolean sneak) {
    boolean old = isSneaking();
    getLocalPlayer().setSneaking(sneak);
    if (getLocalPlayer().movementInput != null) {
      getLocalPlayer().movementInput.sneaking = sneak;
    }
    return old;
  }
  
  public static void setSneakingSuppression(boolean suppress) {
    SneakService.getInstance().setSuppressing(suppress);
  }
  
  public static Vector3d getEyePos() {
    return EntityUtils.getEyePos(getLocalPlayer());
  }
  
  public static Vector3d getDirectionVector() {
    return getViewAngles().getDirectionVector().normalize();
  }
  
  public static Vector3d getServerDirectionVector() {
    return getServerViewAngles().getDirectionVector().normalize();
  }
  
  public static RayTraceResult getViewTrace() {
    return MC.objectMouseOver;
  }
  
  public static BlockRayTraceResult getBlockViewTrace() {
    Vector3d start = getLocalPlayer().getEyePosition(1.f);
    Vector3d end = start.add(getLocalPlayer().getLook(1.f).normalize().scale(getBlockReach()));
    RayTraceContext ctx = new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE,
        RayTraceContext.FluidMode.NONE, getLocalPlayer());
    return getWorld().rayTraceBlocks(ctx);
  }
  
  public static RayTraceResult getViewTrace(Entity entity, Vector3d direction,
      float partialTicks, double reach, double reachAttack) {
    if (entity == null) {
      return null;
    }
    
    Vector3d eyes = entity.getEyePosition(partialTicks);
    RayTraceResult trace = entity.pick(reach, partialTicks, false);
    
    Vector3d dir = direction.scale(reach);
    Vector3d lookDir = eyes.add(dir);
    
    double hitDistance = Type.MISS.equals(trace.getType()) ? reachAttack : trace.getHitVec().distanceTo(eyes);
    Entity hitEntity = null;
    Vector3d hitEntityVec = null;
    
    for (Entity ent : getWorld().getEntitiesInAABBexcluding(entity,
        entity.getBoundingBox().expand(dir.x, dir.y, dir.y).grow(1.D),
        EntityPredicates.NOT_SPECTATING
            .and(Objects::nonNull)
            .and(Entity::canBeCollidedWith))) {
      AxisAlignedBB bb = ent.getBoundingBox().grow(ent.getCollisionBorderSize());
      Vector3d hitVec = bb.rayTrace(eyes, lookDir).orElse(null);
      if (bb.contains(eyes)) {
        if (hitDistance > 0.D) {
          hitEntity = ent;
          hitEntityVec = hitVec == null ? eyes : hitVec;
          hitDistance = 0.D;
        }
      } else if (hitVec != null) {
        double dist = eyes.distanceTo(hitVec);
        if (dist < hitDistance || hitDistance == 0.D) {
          if (entity.getLowestRidingEntity() == ent.getLowestRidingEntity()
              && !ent.canRiderInteract()) {
            if (hitDistance == 0.D) {
              hitEntity = ent;
              hitEntityVec = hitVec;
            }
          } else {
            hitEntity = ent;
            hitEntityVec = hitVec;
            hitDistance = dist;
          }
        }
      }
    }
    
    if (hitEntity != null && reach > 3.D && eyes.distanceTo(hitEntityVec) > 3.D) {
      return new BlockRayTraceResult(hitEntityVec, Direction.UP, new BlockPos(hitEntityVec), false);
    } else if (hitEntity != null && trace == null && hitDistance < reachAttack) {
      return new EntityRayTraceResult(hitEntity, hitEntityVec);
    } else {
      return trace;
    }
  }

  public static double getBlockReach() {
    return getPlayerController().getBlockReachDistance();
  }
  
  public static boolean isInReach(Vector3d start, Vector3d end) {
    return start.squareDistanceTo(end) < getBlockReach() * getBlockReach();
  }
  
  private static final Switch FLY_SWITCH = new Switch("PlayerFlying") {
    @Override
    protected void onEnabled() {
      addScheduledTask(() -> {
        if (getLocalPlayer() == null || getLocalPlayer().abilities == null) {
          return;
        }

        getLocalPlayer().abilities.allowFlying = true;
        getLocalPlayer().abilities.isFlying = true;
      });
    }
    
    @Override
    protected void onDisabled() {
      addScheduledTask(() -> {
        if (getLocalPlayer() == null || getLocalPlayer().abilities == null) {
          return;
        }
        
        PlayerAbilities gmCaps = new PlayerAbilities();
        getPlayerController().getCurrentGameType().configurePlayerCapabilities(gmCaps);
        
        PlayerAbilities capabilities = getLocalPlayer().abilities;
        capabilities.allowFlying = gmCaps.allowFlying;
        capabilities.isFlying = gmCaps.allowFlying && capabilities.isFlying;
        capabilities.setFlySpeed(gmCaps.getFlySpeed());
      });
    }
  };
  
  public static Switch getFlySwitch() {
    return FLY_SWITCH;
  }
}
