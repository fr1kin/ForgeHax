package dev.fiki.forgehax.main.util.entity.mobtypes.standard;

import dev.fiki.forgehax.main.util.common.PriorityEnum;
import dev.fiki.forgehax.main.util.entity.mobtypes.EntityRelationProvider;
import dev.fiki.forgehax.main.util.entity.mobtypes.RelationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.GolemEntity;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class FriendlyRelationProvider extends EntityRelationProvider<Entity> {

  @Override
  protected PriorityEnum getPriority() {
    return PriorityEnum.LOWEST;
  }

  @Override
  public boolean isProviderFor(Entity entity) {
    return EntityClassification.CREATURE.equals(entity.getClassification(false))
        || EntityClassification.WATER_CREATURE.equals(entity.getClassification(false))
        || EntityClassification.AMBIENT.equals(entity.getClassification(false))
        || entity instanceof VillagerEntity
        || entity instanceof GolemEntity;
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
