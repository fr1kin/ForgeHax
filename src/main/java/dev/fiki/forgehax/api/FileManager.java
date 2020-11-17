package dev.fiki.forgehax.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created on 5/30/2017 by fr1kin
 */
public class FileManager {
  private static String[] expandPath(String fullPath) {
    return fullPath.split(":?\\\\\\\\|\\/");
  }
  
  private static Stream<String> expandPaths(String... paths) {
    return Arrays.stream(paths).map(FileManager::expandPath).flatMap(Arrays::stream);
  }
  
  private static Path lookupPath(Path root, String... paths) {
    return Paths.get(root.toString(), paths);
  }
  
  private static Path getRoot() {
    return Paths.get("");
  }
  
  private static void createDirectory(Path dir) {
    try {
      if (!Files.isDirectory(dir)) {
        if (Files.exists(dir)) {
          Files.delete(dir); // delete if it exists but isn't a directory
        }
        
        Files.createDirectories(dir);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private static Path getMkDirectory(Path parent, String... paths) {
    if (paths.length < 1) {
      return parent;
    }
    
    Path dir = lookupPath(parent, paths);
    createDirectory(dir);
    return dir;
  }
  
  private final Path base;

  public FileManager() {
    base = getMkDirectory(getRoot(), "forgehax");
    // create directories for these common
    getMkDirectory(base, "config");
    getMkDirectory(base, "cache");
  }
  
  public Path getBasePath() {
    return base;
  }
  
  public Path getBaseResolve(String... paths) {
    String[] names = expandPaths(paths).toArray(String[]::new);
    if (names.length < 1) {
      throw new IllegalArgumentException("missing path");
    }
    
    return lookupPath(getBasePath(), names);
  }
  
  public Path getMkBaseResolve(String... paths) {
    Path path = getBaseResolve(paths);
    createDirectory(path.getParent());
    return path;
  }
  
  public Path getConfig() {
    return getBasePath().resolve("config");
  }
  
  public Path getCache() {
    return getBasePath().resolve("cache");
  }
  
  public Path getMkBaseDirectory(String... names) {
    return getMkDirectory(
        getBasePath(), expandPaths(names).collect(Collectors.joining(File.separator)));
  }
  
  public Path getMkConfigDirectory(String... names) {
    return getMkDirectory(
        getConfig(), expandPaths(names).collect(Collectors.joining(File.separator)));
  }
}
