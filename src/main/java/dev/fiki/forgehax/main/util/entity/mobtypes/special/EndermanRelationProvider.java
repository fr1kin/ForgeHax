package dev.fiki.forgehax.main.util.entity.mobtypes.special;

import dev.fiki.forgehax.main.util.common.PriorityEnum;
import dev.fiki.forgehax.main.util.entity.mobtypes.EntityRelationProvider;
import dev.fiki.forgehax.main.util.entity.mobtypes.RelationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EndermanEntity;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class EndermanRelationProvider extends EntityRelationProvider<EndermanEntity> {
  
  @Override
  protected PriorityEnum getPriority() {
    return PriorityEnum.DEFAULT;
  }
  
  @Override
  public boolean isProviderFor(Entity entity) {
    return entity instanceof EndermanEntity;
  }

  @Override
  public RelationState getDefaultRelationState() {
    return RelationState.NEUTRAL;
  }

  @Override
  public RelationState getCurrentRelationState(EndermanEntity entity) {
    return entity.isScreaming() ? RelationState.HOSTILE : RelationState.FRIENDLY;
  }
}
