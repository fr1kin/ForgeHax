package com.matt.forgehax;

import com.matt.forgehax.mods.BaseMod;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.Map;

import static com.matt.forgehax.Wrapper.*;

public class ConfigurationHandler implements Globals {
    private static final ConfigurationHandler INSTANCE = new ConfigurationHandler();

    public static ConfigurationHandler getInstance() {
        return INSTANCE;
    }

    private ConfigurationHandler() {}

    public Configuration getConfiguration() {
        return getFileManager().getForgeConfiguration();
    }

    public File getConfigurationFile() {
        return getConfiguration().getConfigFile();
    }

    public String getConfigurationFileName() {
        return getConfigurationFile().getName();
    }

    public void save() {
        if(getConfiguration().hasChanged()) getConfiguration().save();
    }

    public void load() {
        getConfiguration().load();
    }

    public void initialize() {
        load();
        getModManager().getMods().forEach(mod -> mod.initialize(getConfiguration()));
        save();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals(ForgeHax.MODID)) {
            save();
            getModManager().getMods().forEach(BaseMod::update);
        }
    }
}
