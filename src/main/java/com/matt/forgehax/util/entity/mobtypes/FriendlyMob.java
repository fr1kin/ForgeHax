package com.matt.forgehax.util.entity.mobtypes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.GolemEntity;

/** Created on 6/27/2017 by fr1kin */
public class FriendlyMob extends MobType {
  @Override
  public boolean isMobType(Entity entity) {
    return entity.getClassification(false) == EntityClassification.CREATURE
        || entity.getClassification(false) == EntityClassification.AMBIENT
        || entity.getClassification(false) == EntityClassification.WATER_CREATURE
        || entity instanceof VillagerEntity
        || entity instanceof GolemEntity;
  }

  @Override
  protected MobTypeEnum getMobTypeUnchecked(Entity entity) {
    return MobTypeEnum.FRIENDLY;
  }
}
