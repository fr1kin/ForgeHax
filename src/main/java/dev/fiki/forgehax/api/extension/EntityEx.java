package dev.fiki.forgehax.api.extension;

import dev.fiki.forgehax.api.entity.RelationState;
import dev.fiki.forgehax.api.math.Angle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

import static dev.fiki.forgehax.main.Common.*;

public final class EntityEx {
  @SuppressWarnings("unchecked")
  public static RelationState getPlayerRelationship(Entity entity) {
    if (entity instanceof PlayerEntity) {
      return RelationState.PLAYER;
    } else {
      // friendly mobs can be aggressive so we must check that
      return isMobAggressive(entity) ? RelationState.HOSTILE
          : (isPeaceful(entity) ? RelationState.FRIENDLY : RelationState.HOSTILE);
    }
  }

  public static boolean isMobAggressive(Entity entity) {
    // if the owner is the local player, then the entity is (probably) not aggressive towards him
    if (entity instanceof TameableEntity) {
      final UUID owner = ((TameableEntity) entity).getOwnerId();
      if (owner != null && getLocalPlayer() != null && owner.equals(getLocalPlayer().getUniqueID())) {
        return false;
      }
    }

    boolean aggressive = false;

    // this is usually networked, so it will be the primary method of detecting if a mob is angry
    if (entity instanceof MobEntity) {
      aggressive = ((MobEntity) entity).isAggressive();
    }

    // this is usually server side only, but it can be networked for some entities
    if (!aggressive && entity instanceof IAngerable) {
      aggressive = ((IAngerable) entity).func_233678_J__();
    }

    return aggressive;
  }

  /**
   * Check if the mob is an instance of EntityLivingBase
   */
  public static boolean isLivingType(@Nullable Entity entity) {
    return entity instanceof LivingEntity;
  }

  /**
   * If the entity is a player
   */
  public static boolean isPlayerType(Entity entity) {
    return entity instanceof PlayerEntity;
  }

  public static boolean isLocalPlayer(Entity entity) {
    return Objects.equals(getLocalPlayer(), entity);
  }

  public static boolean notLocalPlayer(Entity entity) {
    return !isLocalPlayer(entity);
  }

  public static boolean isValidEntity(Entity entity) {
    Entity riding = getLocalPlayer().getRidingEntity();
    return entity.ticksExisted > 1
        && (riding == null || !riding.equals(entity));
  }

  public static boolean isReallyAlive(Entity entity) {
    return isLivingType(entity) && entity.isAlive() && ((LivingEntity) (entity)).getHealth() > 0;
  }

  /**
   * If the mob is friendly (not aggressive)
   */
  public static boolean isPeaceful(Entity entity) {
    return entity.getClassification(false).getPeacefulCreature();
  }

  /**
   * If the mob is hostile
   */
  public static boolean isHostile(Entity entity) {
    return !entity.getClassification(false).getPeacefulCreature();
  }

  /**
   * Find the entities interpolated amount
   */
  public static Vector3d getInterpolatedAmount(Entity entity, double x, double y, double z) {
    return new Vector3d(
        (entity.getPosX() - entity.lastTickPosX) * x,
        (entity.getPosY() - entity.lastTickPosY) * y,
        (entity.getPosZ() - entity.lastTickPosZ) * z);
  }

  public static Vector3d getInterpolatedAmount(Entity entity, Vector3d vec) {
    return getInterpolatedAmount(entity, vec.x, vec.y, vec.z);
  }

  public static Vector3d getInterpolatedAmount(Entity entity, double ticks) {
    return getInterpolatedAmount(entity, ticks, ticks, ticks);
  }

  /**
   * Find the entities interpolated position
   */
  public static Vector3d getInterpolatedPos(Entity entity, double ticks) {
    double x = MathHelper.lerp(ticks, entity.lastTickPosX, entity.getPosX());
    double y = MathHelper.lerp(ticks, entity.lastTickPosY, entity.getPosY());
    double z = MathHelper.lerp(ticks, entity.lastTickPosZ, entity.getPosZ());
    return new Vector3d(x, y, z);
  }

  public static Vector3d getInterpolatedPos(Entity entity, Vector3d start, double ticks) {
    double x = MathHelper.lerp(ticks, entity.lastTickPosX, start.getX());
    double y = MathHelper.lerp(ticks, entity.lastTickPosY, start.getY());
    double z = MathHelper.lerp(ticks, entity.lastTickPosZ, start.getZ());
    return new Vector3d(x, y, z);
  }

  /**
   * Find the entities interpolated eye position
   */
  public static Vector3d getInterpolatedEyePos(Entity entity, double ticks) {
    return getInterpolatedPos(entity, ticks).add(0, entity.getEyeHeight(), 0);
  }

  /**
   * Get entities eye position
   */
  public static Vector3d getEyePos(Entity entity) {
    return new Vector3d(entity.getPosX(), entity.getPosY() + entity.getEyeHeight(), entity.getPosZ());
  }

  /**
   * Find the center of the entities hit box
   */
  public static Vector3d getOBBCenter(Entity entity) {
    AxisAlignedBB obb = entity.getBoundingBox();
    return new Vector3d(
        (obb.maxX + obb.minX) / 2.D,
        (obb.maxY + obb.minY) / 2.D,
        (obb.maxZ + obb.minZ) / 2.D);
  }

  public static Angle getLookAngles(Entity origin, Vector3d end) {
    return VectorEx.getAngleFacingInDegrees(end.subtract(getEyePos(origin))).normalize();
  }

  public static boolean isDrivenByPlayer(Entity entityIn) {
    return getLocalPlayer() != null && entityIn != null && entityIn == getMountedEntity();
  }

  public static boolean isAboveWater(Entity entity) {
    return isAboveWater(entity, false);
  }

  public static boolean isAboveWater(Entity entity, boolean packet) {
    if (entity == null) {
      return false;
    }

    // increasing this seems to flag more in NCP but needs to be increased
    // so the player lands on solid water
    double y = entity.getPosY() - (packet ? 0.03 : (EntityEx.isPlayerType(entity) ? 0.2 : 0.5));

    for (int x = MathHelper.floor(entity.getPosX()); x < MathHelper.ceil(entity.getPosX()); x++) {
      for (int z = MathHelper.floor(entity.getPosZ()); z < MathHelper.ceil(entity.getPosZ()); z++) {
        BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);

        if (getWorld().getBlockState(pos).getMaterial().isLiquid()) {
          return true;
        }
      }
    }

    return false;
  }

  public static boolean isInWaterMotionState(Entity entity) {
    if (entity == null) {
      return false;
    }

    double y = entity.getPosY() + 0.01;

    for (int x = MathHelper.floor(entity.getPosX()); x < MathHelper.ceil(entity.getPosX()); x++) {
      for (int z = MathHelper.floor(entity.getPosZ()); z < MathHelper.ceil(entity.getPosZ()); z++) {
        BlockPos pos = new BlockPos(x, (int) y, z);

        if (getWorld().getBlockState(pos).getMaterial().isLiquid()) {
          return true;
        }
      }
    }

    return false;
  }

  public static boolean isAboveLand(Entity entity) {
    if (entity == null) {
      return false;
    }

    double y = entity.getPosY() - 0.01;

    for (int x = MathHelper.floor(entity.getPosX()); x < MathHelper.ceil(entity.getPosX()); x++) {
      for (int z = MathHelper.floor(entity.getPosZ()); z < MathHelper.ceil(entity.getPosZ()); z++) {
        BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);
        if (VoxelShapes.fullCube().equals(getWorld().getBlockState(pos).getCollisionShape(getWorld(), pos))) {
          return true;
        }
      }
    }

    return false;
  }
}
