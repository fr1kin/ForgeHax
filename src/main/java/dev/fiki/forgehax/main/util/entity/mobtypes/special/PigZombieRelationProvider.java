package dev.fiki.forgehax.main.util.entity.mobtypes.special;

import dev.fiki.forgehax.main.util.entity.mobtypes.EntityRelationProvider;
import dev.fiki.forgehax.main.util.entity.mobtypes.RelationState;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import dev.fiki.forgehax.main.util.common.PriorityEnum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.ZombiePigmanEntity;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class PigZombieRelationProvider extends EntityRelationProvider<ZombiePigmanEntity> {
  
  @Override
  protected PriorityEnum getPriority() {
    return PriorityEnum.DEFAULT;
  }
  
  @Override
  public boolean isProviderFor(Entity entity) {
    return entity instanceof ZombiePigmanEntity;
  }

  @Override
  public RelationState getDefaultRelationState() {
    return RelationState.NEUTRAL;
  }

  @Override
  public RelationState getCurrentRelationState(ZombiePigmanEntity entity) {
    return (entity.isAggressive() || FastReflection.Fields.ZombiePigmanEntity_angerLevel.get(entity) > 0)
        ? RelationState.HOSTILE
        : RelationState.NEUTRAL;
  }
}
