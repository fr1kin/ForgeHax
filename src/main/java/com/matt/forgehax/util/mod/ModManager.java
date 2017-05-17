package com.matt.forgehax.util.mod;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.matt.forgehax.mods.BaseMod;
import com.matt.forgehax.util.mod.loader.ForgeHaxModLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created on 5/16/2017 by fr1kin
 */
public class ModManager {
    private static final ModManager INSTANCE = new ModManager();

    public static ModManager getInstance() {
        return INSTANCE;
    }

    private final Map<String, BaseMod> mods = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

    private final Set<String> loadPackages = Sets.newHashSet();

    public void registerMod(BaseMod mod) {
        mods.put(mod.getModName(), mod);
    }

    public void unregisterMod(BaseMod mod) {
        mods.remove(mod.getModName());
    }

    public void addPackage(String path) {
        loadPackages.add(path);
    }

    public void removePackage(String path) {
        loadPackages.remove(path);
    }

    public void loadPackages() {
        loadPackages.forEach(dir -> ForgeHaxModLoader.loadClasses(ForgeHaxModLoader.getClassesInPackage(dir)).forEach(this::registerMod));
    }

    public BaseMod getMod(String mod) {
        return mods.get(mod);
    }

    public Collection<BaseMod> getMods() {
        return Collections.unmodifiableCollection(mods.values());
    }
}
