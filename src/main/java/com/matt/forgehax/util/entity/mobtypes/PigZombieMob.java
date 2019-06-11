package com.matt.forgehax.util.entity.mobtypes;

import com.matt.forgehax.util.common.PriorityEnum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.ZombiePigmanEntity;

/** Created on 6/27/2017 by fr1kin */
public class PigZombieMob extends MobType {
  @Override
  protected PriorityEnum getPriority() {
    return PriorityEnum.LOW;
  }

  @Override
  public boolean isMobType(Entity entity) {
    return entity instanceof ZombiePigmanEntity;
  }

  @Override
  // 1.14: armsRaised() removed
  protected MobTypeEnum getMobTypeUnchecked(Entity entity) {
    ZombiePigmanEntity zombie = (ZombiePigmanEntity) entity;
    return (/*zombie.isArmsRaised() ||*/ zombie.isAngry()) ? MobTypeEnum.HOSTILE : MobTypeEnum.NEUTRAL;
  }
}
