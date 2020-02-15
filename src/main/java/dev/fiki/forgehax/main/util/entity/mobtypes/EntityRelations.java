package dev.fiki.forgehax.main.util.entity.mobtypes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dev.fiki.forgehax.main.util.entity.mobtypes.special.EndermanRelationProvider;
import dev.fiki.forgehax.main.util.entity.mobtypes.special.PigZombieRelationProvider;
import dev.fiki.forgehax.main.util.entity.mobtypes.special.WolfRelationProvider;
import dev.fiki.forgehax.main.util.entity.mobtypes.standard.FriendlyRelationProvider;
import dev.fiki.forgehax.main.util.entity.mobtypes.standard.HostileRelationProvider;
import dev.fiki.forgehax.main.util.entity.mobtypes.standard.PlayerRelationProvider;
import net.minecraft.entity.Entity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class EntityRelations {
  private static final List<EntityRelationProvider<?>> REGISTRY = Lists.newArrayList();
  private static List<EntityRelationProvider<?>> readOnly = ImmutableList.of();
  
  public static void register(EntityRelationProvider<?> type) {
    synchronized (REGISTRY) {
      REGISTRY.add(type);
      Collections.sort(REGISTRY);
      readOnly = ImmutableList.copyOf(REGISTRY);
    }
  }
  
  public static void unregister(EntityRelationProvider<?> type) {
    synchronized (REGISTRY) {
      REGISTRY.remove(type);
      Collections.sort(REGISTRY);
      readOnly = ImmutableList.copyOf(REGISTRY);
    }
  }
  
  public static List<EntityRelationProvider<?>> getRegistry() {
    return readOnly;
  }

  public static Optional<EntityRelationProvider> getProvider(Entity entity) {
    for(EntityRelationProvider<?> type : getRegistry()) {
      if(type.isProviderFor(entity)) {
        return Optional.of(type);
      }
    }
    return Optional.empty();
  }
  
  static {
    // most important
    register(new PlayerRelationProvider());

    // mobs that change state depending on their state
    register(new EndermanRelationProvider());
    register(new PigZombieRelationProvider());
    register(new WolfRelationProvider());

    // these should be at the bottom
    register(new FriendlyRelationProvider());
    register(new HostileRelationProvider());
  }
}
