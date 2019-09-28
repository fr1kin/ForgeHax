package com.matt.forgehax.util.entity.mobtypes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class MobTypeRegistry {
  
  public static final MobType HOSTILE = new HostileMob();
  public static final MobType FRIENDLY = new FriendlyMob();
  
  private static final List<MobType> MOB_TYPES_SPECIAL = Lists.newArrayList();
  private static List<MobType> readOnly = ImmutableList.of();
  
  public static void register(MobType type) {
    synchronized (MOB_TYPES_SPECIAL) {
      MOB_TYPES_SPECIAL.add(type);
      Collections.sort(MOB_TYPES_SPECIAL);
      readOnly = ImmutableList.copyOf(MOB_TYPES_SPECIAL);
    }
  }
  
  public static void unregister(MobType type) {
    synchronized (MOB_TYPES_SPECIAL) {
      MOB_TYPES_SPECIAL.remove(type);
      Collections.sort(MOB_TYPES_SPECIAL);
      readOnly = ImmutableList.copyOf(MOB_TYPES_SPECIAL);
    }
  }
  
  public static List<MobType> getSortedSpecialMobTypes() {
    return readOnly;
  }
  
  static {
    register(new EndermanMob());
    register(new PigZombieMob());
    register(new WolfMob());
  }
}
