package dev.fiki.forgehax.main.util.entity;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.entity.mobtypes.EntityRelations;
import dev.fiki.forgehax.main.util.entity.mobtypes.RelationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Objects;

import static dev.fiki.forgehax.main.Common.*;

public class EntityUtils implements Common {

  @SuppressWarnings("unchecked")
  public static RelationState getRelationship(Entity entity) {
    return EntityRelations.getProvider(entity)
        .map(provider -> provider.getCurrentRelationState(entity))
        .orElse(RelationState.HOSTILE);
  }

  public static boolean isBatsDisabled = false;

  public static boolean isZombiePigmanAggressive(ZombifiedPiglinEntity entity) {
    return entity.getAngerTime() > 0;
  }

  /**
   * Checks if the mob could be possibly hostile towards us (we can't detect their attack target
   * easily) Current entities: PigZombie: Aggressive if arms are raised, when arms are put down a
   * internal timer is slowly ticked down from 400 Wolf: Aggressive if the owner isn't the local
   * player and the wolf is angry Enderman: Aggressive if making screaming sounds
   */
  public static boolean isMobAggressive(Entity entity) {
    if (entity instanceof ZombifiedPiglinEntity) {
      // arms raised = aggressive, angry = either game or we have set the anger cooldown
      if (((ZombifiedPiglinEntity) entity).isAggressive() || isZombiePigmanAggressive((ZombifiedPiglinEntity) entity)) {
        if (!isZombiePigmanAggressive((ZombifiedPiglinEntity) entity)) {
          // set pigmens anger to 400 if it hasn't been angered already
          ((IAngerable) entity).setAngerTime(400);
        }
        return true;
      }
    } else if (entity instanceof WolfEntity) {
      return ((WolfEntity) entity).getAngerTime() > 0 && !getLocalPlayer().equals(((WolfEntity) entity).getOwner());
    } else if (entity instanceof EndermanEntity) {
      return ((EndermanEntity) entity).isScreaming();
    }
    return false;
  }

  /**
   * Check if the mob is an instance of EntityLivingBase
   */
  public static boolean isLiving(Entity entity) {
    return entity instanceof LivingEntity;
  }

  /**
   * If the entity is a player
   */
  public static boolean isPlayer(Entity entity) {
    return entity instanceof PlayerEntity;
  }

  public static boolean isLocalPlayer(Entity entity) {
    return Objects.equals(getLocalPlayer(), entity);
  }

  public static boolean notLocalPlayer(Entity entity) {
    return !isLocalPlayer(entity);
  }

  public static boolean isFakeLocalPlayer(Entity entity) {
    return entity != null && entity.getEntityId() == -100;
  }

  public static boolean isValidEntity(Entity entity) {
    Entity riding = getLocalPlayer().getRidingEntity();
    return entity.ticksExisted > 1
        && !isFakeLocalPlayer(entity)
        && (riding == null || !riding.equals(entity));
  }

  public static boolean isAlive(Entity entity) {
    return isLiving(entity) && entity.isAlive() && ((LivingEntity) (entity)).getHealth() > 0;
  }

  /**
   * If the mob by default wont attack the player, but will if the player attacks it
   */
  public static boolean isNeutralMob(Entity entity) {
    return entity instanceof ZombifiedPiglinEntity
        || entity instanceof WolfEntity
        || entity instanceof EndermanEntity;
  }

  /**
   * If the mob is friendly (not aggressive)
   */
  public static boolean isFriendlyMob(Entity entity) {
    return (EntityClassification.CREATURE.equals(entity.getClassification(false))
        && !EntityUtils.isNeutralMob(entity))
        || (EntityClassification.AMBIENT.equals(entity.getClassification(false)) && !isBatsDisabled)
        || entity instanceof VillagerEntity
        || entity instanceof IronGolemEntity
        || (isNeutralMob(entity) && !EntityUtils.isMobAggressive(entity));
  }

  /**
   * If the mob is hostile
   */
  public static boolean isHostileMob(Entity entity) {
    return (EntityClassification.MONSTER.equals(entity.getClassification(false))
        && !EntityUtils.isNeutralMob(entity))
        || EntityUtils.isMobAggressive(entity);
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
        (obb.maxX + obb.minX) / 2.D, (obb.maxY + obb.minY) / 2.D, (obb.maxZ + obb.minZ) / 2.D);
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

    double y =
        entity.getPosY()
            - (packet
            ? 0.03
            : (EntityUtils.isPlayer(entity)
            ? 0.2
            : 0.5)); // increasing this seems to flag more in NCP but needs to be increased
    // so the player lands on solid water

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

  public static boolean isInWater(Entity entity) {
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
}
