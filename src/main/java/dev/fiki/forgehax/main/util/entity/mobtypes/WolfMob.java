package dev.fiki.forgehax.main.util.entity.mobtypes;

import dev.fiki.forgehax.main.util.common.PriorityEnum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.WolfEntity;

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
    WolfEntity wolf = (WolfEntity) entity;
    return wolf.isAngry() ? MobTypeEnum.HOSTILE : MobTypeEnum.NEUTRAL;
  }
  
  @Override
  public boolean isMobType(Entity entity) {
    return entity instanceof WolfEntity;
  }
}
