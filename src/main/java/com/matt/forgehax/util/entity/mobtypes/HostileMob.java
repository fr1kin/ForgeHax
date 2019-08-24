package com.matt.forgehax.util.entity.mobtypes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class HostileMob extends MobType {
  
  @Override
  public boolean isMobType(Entity entity) {
    return entity.isCreatureType(EnumCreatureType.MONSTER, false);
  }

  @Override
  protected MobTypeEnum getMobTypeUnchecked(Entity entity) {
    return MobTypeEnum.HOSTILE;
  }
}
