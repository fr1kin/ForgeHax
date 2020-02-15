package dev.fiki.forgehax.main.util.entity.mobtypes;

import dev.fiki.forgehax.main.util.common.PriorityEnum;
import net.minecraft.entity.Entity;

/**
 * Created on 6/27/2017 by fr1kin
 */
public abstract class EntityRelationProvider<E extends Entity> implements Comparable<EntityRelationProvider> {
  
  protected abstract PriorityEnum getPriority();
  
  public abstract boolean isProviderFor(Entity entity);

  public abstract RelationState getDefaultRelationState();
  
  public abstract RelationState getCurrentRelationState(E entity);
  
  @Override
  public int compareTo(EntityRelationProvider o) {
    return getPriority().compareTo(o.getPriority());
  }
}
