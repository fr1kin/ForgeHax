package com.matt.forgehax.util.mod;

import com.google.common.collect.Maps;
import com.matt.forgehax.mods.BaseMod;
import net.minecraftforge.common.config.Configuration;

import java.util.Collections;
import java.util.Map;

public class ModList {
    private static final ModPropertyList EMPTY_MOD = new ModPropertyList(new BaseMod("null", "null") {});

    private final Map<String, ModPropertyList> mods = Maps.newTreeMap();

    public Map<String, ModPropertyList> getMods() {
        return Collections.unmodifiableMap(mods);
    }

    public void register(BaseMod mod) {
        mods.put(mod.getModName(), new ModPropertyList(mod));
    }

    public ModPropertyList getMod(String name) {
        ModPropertyList r = mods.get(name);
        return r != null ? r : EMPTY_MOD;
    }
}
