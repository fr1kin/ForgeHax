package com.matt.forgehax.util.entity.mobtypes;

import com.matt.forgehax.util.common.PriorityEnum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityWolf;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class WolfMob extends MobType {
  
  @Override
  protected PriorityEnum getPriority() {
    return PriorityEnum.LOW;
  }

  @Override
  protected MobTypeEnum getMobTypeUnchecked(Entity entity) {
    EntityWolf wolf = (EntityWolf) entity;
    return wolf.isAngry() ? MobTypeEnum.HOSTILE : MobTypeEnum.NEUTRAL;
  }

  @Override
  public boolean isMobType(Entity entity) {
    return entity instanceof EntityWolf;
  }
}
