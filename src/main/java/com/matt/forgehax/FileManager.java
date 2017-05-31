package com.matt.forgehax;

import com.matt.forgehax.util.command.CommandLine;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Created on 5/30/2017 by fr1kin
 */
public class FileManager {
    private static final FileManager INSTANCE = new FileManager();

    public static FileManager getInstance() {
        return INSTANCE;
    }

    private File baseDirectory;
    private File configDirectory;
    private Configuration forgeConfiguration;

    private FileManager() {}

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(File baseDirectory) {
        baseDirectory.mkdirs();
        this.baseDirectory = baseDirectory;
    }

    public File getConfigDirectory() {
        return configDirectory;
    }

    public void setConfigDirectory(File configDirectory) {
        configDirectory.mkdirs();
        this.configDirectory = configDirectory;
    }

    public Configuration getForgeConfiguration() {
        return forgeConfiguration;
    }

    public void setForgeConfiguration(Configuration forgeConfiguration) {
        this.forgeConfiguration = forgeConfiguration;
    }

    public File getFileInBaseDirectory(String... paths) {
        return new File(baseDirectory, CommandLine.join(paths, File.separator));
    }

    public File getFileInConfigDirectory(String... paths) {
        return new File(configDirectory, CommandLine.join(paths, File.separator));
    }
}
