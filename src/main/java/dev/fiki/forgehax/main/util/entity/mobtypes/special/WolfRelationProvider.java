package dev.fiki.forgehax.main.util.entity.mobtypes.special;

import dev.fiki.forgehax.main.util.common.PriorityEnum;
import dev.fiki.forgehax.main.util.entity.mobtypes.EntityRelationProvider;
import dev.fiki.forgehax.main.util.entity.mobtypes.RelationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.WolfEntity;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class WolfRelationProvider extends EntityRelationProvider<WolfEntity> {
  
  @Override
  protected PriorityEnum getPriority() {
    return PriorityEnum.DEFAULT;
  }

  @Override
  public boolean isProviderFor(Entity entity) {
    return entity instanceof WolfEntity;
  }

  @Override
  public RelationState getDefaultRelationState() {
    return RelationState.NEUTRAL;
  }

  @Override
  public RelationState getCurrentRelationState(WolfEntity entity) {
    return entity.getAngerTime() > 0 && !entity.isOwner(getLocalPlayer()) ? RelationState.HOSTILE : RelationState.NEUTRAL;
  }
}
