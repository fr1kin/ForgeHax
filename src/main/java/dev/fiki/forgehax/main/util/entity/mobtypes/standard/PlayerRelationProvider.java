package dev.fiki.forgehax.main.util.entity.mobtypes.standard;

import dev.fiki.forgehax.main.util.common.PriorityEnum;
import dev.fiki.forgehax.main.util.entity.mobtypes.EntityRelationProvider;
import dev.fiki.forgehax.main.util.entity.mobtypes.RelationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerRelationProvider extends EntityRelationProvider<PlayerEntity> {
  @Override
  protected PriorityEnum getPriority() {
    return PriorityEnum.HIGHEST;
  }

  @Override
  public boolean isProviderFor(Entity entity) {
    return entity instanceof PlayerEntity;
  }

  @Override
  public RelationState getDefaultRelationState() {
    return RelationState.PLAYER;
  }

  @Override
  public RelationState getCurrentRelationState(PlayerEntity entity) {
    return RelationState.PLAYER;
  }
}
