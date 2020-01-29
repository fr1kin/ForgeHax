package com.matt.forgehax.util.entity.mobtypes;

import com.matt.forgehax.util.common.PriorityEnum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EndermanEntity;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class EndermanMob extends MobType {
  
  @Override
  protected PriorityEnum getPriority() {
    return PriorityEnum.LOW;
  }
  
  @Override
  public boolean isMobType(Entity entity) {
    return entity instanceof EndermanEntity;
  }
  
  @Override
  protected MobTypeEnum getMobTypeUnchecked(Entity entity) {
    EndermanEntity enderman = (EndermanEntity) entity;
    return enderman.isScreaming() ? MobTypeEnum.HOSTILE : MobTypeEnum.NEUTRAL;
  }
}
