package com.matt.forgehax.util.entity.mobtypes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.passive.EntityVillager;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class FriendlyMob extends MobType {
  
  @Override
  public boolean isMobType(Entity entity) {
    return entity.isCreatureType(EnumCreatureType.CREATURE, false)
      || entity.isCreatureType(EnumCreatureType.AMBIENT, false)
      || entity.isCreatureType(EnumCreatureType.WATER_CREATURE, false)
      || entity instanceof EntityVillager
      || entity instanceof EntityGolem;
  }
  
  @Override
  protected MobTypeEnum getMobTypeUnchecked(Entity entity) {
    return MobTypeEnum.FRIENDLY;
  }
}
