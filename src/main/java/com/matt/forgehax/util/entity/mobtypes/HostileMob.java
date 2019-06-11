package com.matt.forgehax.util.entity.mobtypes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;


/** Created on 6/27/2017 by fr1kin */
public class HostileMob extends MobType {
  @Override
  // 1.14: this should be correct
  public boolean isMobType(Entity entity) {
    return entity.getType().getClassiciation() == EntityClassification.MONSTER;
  }

  @Override
  protected MobTypeEnum getMobTypeUnchecked(Entity entity) {
    return MobTypeEnum.HOSTILE;
  }
}
