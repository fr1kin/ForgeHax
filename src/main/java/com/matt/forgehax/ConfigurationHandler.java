package com.matt.forgehax;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

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
        getModManager().getMods().forEach(mod -> mod.load(getConfiguration()));
        save();
    }
}
