package dev.fiki.forgehax.main.util.entity.mobtypes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class HostileMob extends MobType {
  
  @Override
  public boolean isMobType(Entity entity) {
    return EntityClassification.MONSTER.equals(entity.getClassification(false));
  }
  
  @Override
  protected MobTypeEnum getMobTypeUnchecked(Entity entity) {
    return MobTypeEnum.HOSTILE;
  }
}
