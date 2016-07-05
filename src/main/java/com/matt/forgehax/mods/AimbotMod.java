package com.matt.forgehax.mods;

import net.minecraftforge.common.config.Configuration;

public class AimbotMod extends ToggleMod {

    public AimbotMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings();
    }
}
