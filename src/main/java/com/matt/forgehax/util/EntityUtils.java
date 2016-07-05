package com.matt.forgehax.util;

import com.matt.forgehax.ForgeHaxBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EntityUtils extends ForgeHaxBase {
    public static boolean isBatsDisabled = false;

    /**
     * Checks if the mob could be possibly hostile towards us (we can't detect their attack target easily)
     * Current entities:
     *                  PigZombie: Aggressive if arms are raised, when arms are put down a internal timer is slowly ticked down from 400
     *                  Wolf: Aggressive if the owner isn't the local player and the wolf is angry
     *                  Enderman: Aggressive if making screaming sounds
     */
    public static boolean isMobAggressive(Entity entity) {
        if(entity instanceof EntityPigZombie) {
            // arms raised = aggressive, angry = either game or we have set the anger cooldown
            if(((EntityPigZombie) entity).isArmsRaised() || ((EntityPigZombie) entity).isAngry()) {
                if(!((EntityPigZombie) entity).isAngry()) {
                    // set pigmens anger to 400 if it hasn't been angered already
                    ((EntityPigZombie) entity).angerLevel = 400;
                }
                return true;
            }
        } else if(entity instanceof EntityWolf) {
            return ((EntityWolf) entity).isAngry() &&
                    !MC.thePlayer.equals(((EntityWolf) entity).getOwner());
        } else if(entity instanceof EntityEnderman) {
            return ((EntityEnderman) entity).isScreaming();
        }
        return false;
    }

    /**
     * Check if the mob is an instance of EntityLivingBase
     */
    public static boolean isLiving(Entity entity) {
        return entity instanceof EntityLivingBase;
    }

    /**
     * If the entity is a player
     */
    public static boolean isPlayer(Entity entity) {
        return entity instanceof EntityPlayer;
    }

    public static boolean isValidEntity(Entity entity) {
        return entity.ticksExisted > 1;
    }

    /**
     * If the mob by default wont attack the player, but will if the player attacks it
     */
    public static boolean isNeutralMob(Entity entity) {
        return entity instanceof EntityPigZombie ||
                entity instanceof EntityWolf ||
                entity instanceof EntityEnderman;
    }

    /**
     * If the mob is friendly (not aggressive)
     */
    public static boolean isFriendlyMob(Entity entity) {
        return (entity.isCreatureType(EnumCreatureType.CREATURE, false) && !EntityUtils.isNeutralMob(entity)) ||
                (entity.isCreatureType(EnumCreatureType.AMBIENT, false) && !isBatsDisabled) ||
                entity instanceof EntityVillager ||
                entity instanceof EntityIronGolem ||
                (isNeutralMob(entity) && !EntityUtils.isMobAggressive(entity));
    }

    /**
     * If the mob is hostile
     */
    public static boolean isHostileMob(Entity entity) {
        return (entity.isCreatureType(EnumCreatureType.MONSTER, false) && !EntityUtils.isNeutralMob(entity)) ||
                EntityUtils.isMobAggressive(entity);
    }

    public static Vec3d getRenderPos(Entity entity, float partialTicks) {
        return new Vec3d(
                entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks,
                entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks,
                entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
        );
    }

    public static int getDrawColor(EntityLivingBase living) {
        if(isPlayer(living)) {
            return Utils.toRGBA(255, 0, 0, 255);
        } else if(isHostileMob(living)) {
            return Utils.toRGBA(255, 128, 0, 255);
        } else if(isFriendlyMob(living)) {
            return Utils.toRGBA(0, 255, 0, 255);
        } else {
            return Utils.toRGBA(255, 255, 255, 255);
        }
    }
}
