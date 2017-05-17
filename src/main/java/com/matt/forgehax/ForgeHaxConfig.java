package com.matt.forgehax;

import com.matt.forgehax.mods.BaseMod;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.Map;

import static com.matt.forgehax.Wrapper.*;

public class ForgeHaxConfig implements Globals {
    public Configuration config;

    public ForgeHaxConfig(File file)
    {
        config = new Configuration(file);
        config.load();
        // initialize mod configs
        getModManager().getMods().forEach(mod -> mod.initialize(config));
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
            getModManager().getMods().forEach(BaseMod::update);
        }
    }
}
