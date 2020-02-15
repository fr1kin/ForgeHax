package dev.fiki.forgehax.main.util.entity.mobtypes.standard;

import dev.fiki.forgehax.main.util.common.PriorityEnum;
import dev.fiki.forgehax.main.util.entity.mobtypes.EntityRelationProvider;
import dev.fiki.forgehax.main.util.entity.mobtypes.RelationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class HostileRelationProvider extends EntityRelationProvider<Entity> {

  @Override
  protected PriorityEnum getPriority() {
    return PriorityEnum.LOWEST;
  }

  @Override
  public boolean isProviderFor(Entity entity) {
    return EntityClassification.MONSTER.equals(entity.getClassification(false));
  }

  @Override
  public RelationState getDefaultRelationState() {
    return RelationState.HOSTILE;
  }

  @Override
  public RelationState getCurrentRelationState(Entity entity) {
    return RelationState.HOSTILE;
  }
}
