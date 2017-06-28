package com.matt.forgehax.util.entity.mobtypes;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class MobTypeRegistry {
    private static final List<MobType> MOB_TYPES = Lists.newCopyOnWriteArrayList();

    public static void register(MobType type) {
        MOB_TYPES.add(type);
        Collections.sort(MOB_TYPES);
    }

    public static void unregister(MobType type) {
        MOB_TYPES.remove(type);
        Collections.sort(MOB_TYPES);
    }

    public static List<MobType> getSortedMobTypes() {
        return MOB_TYPES;
    }

    static {
        // default mobs
        register(new HostileMob());
        register(new FriendlyMob());

        // special mobs
        register(new EndermanMob());
        register(new PigZombieMob());
        register(new WolfMob());
    }
}
