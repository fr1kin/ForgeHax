package com.matt.forgehax.util.entity;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getRidingEntity;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.Globals;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.entity.mobtypes.MobType;
import com.matt.forgehax.util.entity.mobtypes.MobTypeEnum;
import com.matt.forgehax.util.entity.mobtypes.MobTypeRegistry;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

public class EntityUtils implements Globals {
  public static MobTypeEnum getRelationship(Entity entity) {
    if (entity instanceof AbstractClientPlayer) return MobTypeEnum.PLAYER;
    else {
      // check special cases first
      for (MobType type : MobTypeRegistry.getSortedSpecialMobTypes())
        if (type.isMobType(entity)) return type.getMobType(entity);
      // this code will continue if no special was found
      if (MobTypeRegistry.HOSTILE.isMobType(entity)) return MobTypeEnum.HOSTILE;
      else if (MobTypeRegistry.FRIENDLY.isMobType(entity)) return MobTypeEnum.FRIENDLY;
      else return MobTypeEnum.HOSTILE; // default to hostile
    }
  }

  public static boolean isBatsDisabled = false;

  /**
   * Checks if the mob could be possibly hostile towards us (we can't detect their attack target
   * easily) Current entities: PigZombie: Aggressive if arms are raised, when arms are put down a
   * internal timer is slowly ticked down from 400 Wolf: Aggressive if the owner isn't the local
   * player and the wolf is angry Enderman: Aggressive if making screaming sounds
   */
  public static boolean isMobAggressive(Entity entity) {
    if (entity instanceof EntityPigZombie) {
      // arms raised = aggressive, angry = either game or we have set the anger cooldown
      if (((EntityPigZombie) entity).isArmsRaised() || ((EntityPigZombie) entity).isAngry()) {
        if (!((EntityPigZombie) entity).isAngry()) {
          // set pigmens anger to 400 if it hasn't been angered already
          FastReflection.Fields.EntityPigZombie_angerLevel.set((EntityPigZombie) entity, 400);
        }
        return true;
      }
    } else if (entity instanceof EntityWolf) {
      return ((EntityWolf) entity).isAngry() && !MC.player.equals(((EntityWolf) entity).getOwner());
    } else if (entity instanceof EntityEnderman) {
      return ((EntityEnderman) entity).isScreaming();
    }
    return false;
  }

  /** Check if the mob is an instance of EntityLivingBase */
  public static boolean isLiving(Entity entity) {
    return entity instanceof EntityLivingBase;
  }

  /** If the entity is a player */
  public static boolean isPlayer(Entity entity) {
    return entity instanceof EntityPlayer;
  }

  public static boolean isLocalPlayer(Entity entity) {
    return Objects.equals(getLocalPlayer(), entity);
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
    return isLiving(entity) && !entity.isDead && ((EntityLivingBase) (entity)).getHealth() > 0;
  }

  /** If the mob by default wont attack the player, but will if the player attacks it */
  public static boolean isNeutralMob(Entity entity) {
    return entity instanceof EntityPigZombie
        || entity instanceof EntityWolf
        || entity instanceof EntityEnderman;
  }

  /** If the mob is friendly (not aggressive) */
  public static boolean isFriendlyMob(Entity entity) {
    return (entity.isCreatureType(EnumCreatureType.CREATURE, false)
            && !EntityUtils.isNeutralMob(entity))
        || (entity.isCreatureType(EnumCreatureType.AMBIENT, false) && !isBatsDisabled)
        || entity instanceof EntityVillager
        || entity instanceof EntityIronGolem
        || (isNeutralMob(entity) && !EntityUtils.isMobAggressive(entity));
  }

  /** If the mob is hostile */
  public static boolean isHostileMob(Entity entity) {
    return (entity.isCreatureType(EnumCreatureType.MONSTER, false)
            && !EntityUtils.isNeutralMob(entity))
        || EntityUtils.isMobAggressive(entity);
  }

  /** Find the entities interpolated amount */
  public static Vec3d getInterpolatedAmount(Entity entity, double x, double y, double z) {
    return new Vec3d(
        (entity.posX - entity.lastTickPosX) * x,
        (entity.posY - entity.lastTickPosY) * y,
        (entity.posZ - entity.lastTickPosZ) * z);
  }

  public static Vec3d getInterpolatedAmount(Entity entity, Vec3d vec) {
    return getInterpolatedAmount(entity, vec.x, vec.y, vec.z);
  }

  public static Vec3d getInterpolatedAmount(Entity entity, double ticks) {
    return getInterpolatedAmount(entity, ticks, ticks, ticks);
  }

  /** Find the entities interpolated position */
  public static Vec3d getInterpolatedPos(Entity entity, double ticks) {
    return new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ)
        .add(getInterpolatedAmount(entity, ticks));
  }

  /** Find the entities interpolated eye position */
  public static Vec3d getInterpolatedEyePos(Entity entity, double ticks) {
    return getInterpolatedPos(entity, ticks).addVector(0, entity.getEyeHeight(), 0);
  }

  /** Get entities eye position */
  public static Vec3d getEyePos(Entity entity) {
    return new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
  }

  /** Find the center of the entities hit box */
  public static Vec3d getOBBCenter(Entity entity) {
    AxisAlignedBB obb = entity.getEntityBoundingBox();
    return new Vec3d(
        (obb.maxX + obb.minX) / 2.D, (obb.maxY + obb.minY) / 2.D, (obb.maxZ + obb.minZ) / 2.D);
  }

  /** Create a trace */
  public static RayTraceResult traceEntity(
      World world, Vec3d start, Vec3d end, List<Entity> filter) {
    RayTraceResult result = null;
    double hitDistance = -1;

    for (Entity ent : world.loadedEntityList) {

      if (filter.contains(ent)) continue;

      double distance = start.distanceTo(ent.getPositionVector());
      RayTraceResult trace = ent.getEntityBoundingBox().calculateIntercept(start, end);

      if (trace != null && (hitDistance == -1 || distance < hitDistance)) {
        hitDistance = distance;
        result = trace;
        result.entityHit = ent;
      }
    }

    return result;
  }

  /** Find the entities draw color */
  public static int getDrawColor(EntityLivingBase living) {
    if (isPlayer(living)) {
      if (PlayerUtils.isFriend((EntityPlayer) living)) return Utils.Colors.GREEN;
      else return Utils.Colors.RED;
    } else if (isHostileMob(living)) {
      return Utils.Colors.ORANGE;
    } else if (isFriendlyMob(living)) {
      return Utils.Colors.GREEN;
    } else {
      return Utils.Colors.WHITE;
    }
  }

  public static boolean isDrivenByPlayer(Entity entityIn) {
    return getLocalPlayer() != null && entityIn != null && entityIn == getRidingEntity();
  }

  public static boolean isAboveWater(Entity entity) {
    return isAboveWater(entity, false);
  }

  public static boolean isAboveWater(Entity entity, boolean packet) {
    if (entity == null) return false;

    double y =
        entity.posY
            - (packet
                ? 0.03
                : (EntityUtils.isPlayer(entity)
                    ? 0.2
                    : 0.5)); // increasing this seems to flag more in NCP but needs to be increased
    // so the player lands on solid water

    for (int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); x++)
      for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); z++) {
        BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);

        if (getWorld().getBlockState(pos).getBlock() instanceof BlockLiquid) return true;
      }

    return false;
  }

  public static boolean isInWater(Entity entity) {
    if (entity == null) return false;

    double y = entity.posY + 0.01;

    for (int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); x++)
      for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); z++) {
        BlockPos pos = new BlockPos(x, (int) y, z);

        if (getWorld().getBlockState(pos).getBlock() instanceof BlockLiquid) return true;
      }

    return false;
  }
}
