package com.matt.forgehax;

import com.matt.forgehax.mods.BaseMod;
import com.matt.forgehax.mods.ToggleMod;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.Map;

public class ForgeHaxConfig extends ForgeHaxBase {
    public Configuration config;

    public ForgeHaxConfig(File file)
    {
        config = new Configuration(file);
        config.load();
        // initialize mod configs
        for(Map.Entry<String,BaseMod> entry : MOD.mods.entrySet()) {
            entry.getValue().initialize(config);
        }
        save();
    }

    public void save()
    {
        if(config.hasChanged()) config.save();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if(event.getModID().equals(ForgeHax.MODID)) {
            save();
            for(Map.Entry<String,BaseMod> entry : MOD.mods.entrySet()) {
                entry.getValue().update();
            }
        }
    }
}
