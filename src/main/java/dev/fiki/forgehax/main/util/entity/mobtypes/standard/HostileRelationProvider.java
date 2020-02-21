package dev.fiki.forgehax.main.util.entity.mobtypes.standard;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.main.util.common.PriorityEnum;
import dev.fiki.forgehax.main.util.entity.mobtypes.EntityRelationProvider;
import dev.fiki.forgehax.main.util.entity.mobtypes.RelationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.ShulkerEntity;

import java.util.Set;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class HostileRelationProvider extends EntityRelationProvider<Entity> {
  private final Set<EntityType> hostileTypes = Sets.newHashSet();

  {
    hostileTypes.add(EntityType.SHULKER);
    hostileTypes.add(EntityType.SHULKER_BULLET);
  }

  @Override
  protected PriorityEnum getPriority() {
    return PriorityEnum.LOWEST;
  }

  @Override
  public boolean isProviderFor(Entity entity) {
    return !entity.getClassification(false).getPeacefulCreature()
        || hostileTypes.contains(entity.getType());
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
