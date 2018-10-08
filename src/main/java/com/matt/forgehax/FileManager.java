package com.matt.forgehax;

import com.matt.forgehax.util.command.CommandHelper;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created on 5/30/2017 by fr1kin
 */
public class FileManager {
    private static final FileManager INSTANCE = new FileManager();

    public static FileManager getInstance() {
        return INSTANCE;
    }

    public static File getWorkingDir() {
        return Paths.get("").toAbsolutePath().toFile();
    }

    private final File baseDirectory;
    private final File configDirectory;
    private final File cacheDirectory;

    private FileManager() {
        baseDirectory = new File(getWorkingDir(), "forgehax");
        baseDirectory.mkdirs();
        configDirectory = getFileInBaseDirectory("config");
        configDirectory.mkdirs();
        cacheDirectory = getFileInBaseDirectory("cache");
        cacheDirectory.mkdir();
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public File getConfigDirectory() {
        return configDirectory;
    }

    public File getCacheDirectory() {
        return cacheDirectory;
    }


    public File getFileInBaseDirectory(String... paths) {
        return new File(baseDirectory, CommandHelper.join(paths, File.separator));
    }

    public File getFileInConfigDirectory(String... paths) {
        return new File(configDirectory, CommandHelper.join(paths, File.separator));
    }
}
