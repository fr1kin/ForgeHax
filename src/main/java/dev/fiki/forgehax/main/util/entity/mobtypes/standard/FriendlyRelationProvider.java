package dev.fiki.forgehax.main.util.entity.mobtypes.standard;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.main.util.common.PriorityEnum;
import dev.fiki.forgehax.main.util.entity.mobtypes.EntityRelationProvider;
import dev.fiki.forgehax.main.util.entity.mobtypes.RelationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.GolemEntity;

import java.util.Set;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class FriendlyRelationProvider extends EntityRelationProvider<Entity> {
  private final Set<EntityType> friendlyTypes = Sets.newHashSet();

  {
    friendlyTypes.add(EntityType.IRON_GOLEM);
    friendlyTypes.add(EntityType.VILLAGER);
  }

  @Override
  protected PriorityEnum getPriority() {
    return PriorityEnum.LOWEST;
  }

  @Override
  public boolean isProviderFor(Entity entity) {
    return entity.getClassification(false).getPeacefulCreature()
        || friendlyTypes.contains(entity.getType());
  }

  @Override
  public RelationState getDefaultRelationState() {
    return RelationState.FRIENDLY;
  }

  @Override
  public RelationState getCurrentRelationState(Entity entity) {
    return RelationState.FRIENDLY;
  }
}
