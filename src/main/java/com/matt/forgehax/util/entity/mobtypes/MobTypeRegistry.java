package com.matt.forgehax.util.entity.mobtypes;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

/** Created on 6/27/2017 by fr1kin */
public class MobTypeRegistry {
  public static final MobType HOSTILE = new HostileMob();
  public static final MobType FRIENDLY = new FriendlyMob();

  private static final List<MobType> MOB_TYPES_SPECIAL = Lists.newCopyOnWriteArrayList();

  public static void register(MobType type) {
    MOB_TYPES_SPECIAL.add(type);
    Collections.sort(MOB_TYPES_SPECIAL);
  }

  public static void unregister(MobType type) {
    MOB_TYPES_SPECIAL.remove(type);
    Collections.sort(MOB_TYPES_SPECIAL);
  }

  public static List<MobType> getSortedSpecialMobTypes() {
    return MOB_TYPES_SPECIAL;
  }

  static {
    register(new EndermanMob());
    register(new PigZombieMob());
    register(new WolfMob());
  }
}
