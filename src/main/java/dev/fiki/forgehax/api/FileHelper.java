package dev.fiki.forgehax.api;

import joptsimple.internal.Strings;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Created on 2/16/2018 by fr1kin */
public class FileHelper {
  private static final Pattern PATTERN_PACKAGE_FROM_PATH =
      Pattern.compile("[.]jar[!][\\/|\\\\](.*)");
  private static final Pattern PATTERN_JAR_DIR_FROM_PATH = Pattern.compile("(.*.jar)");
  private static final Pattern PATTERN_FILE_SEPARATORS = Pattern.compile("[\\\\|\\/]");

  public static String getFileExtension(String fullName) {
    return com.google.common.io.Files.getFileExtension(fullName);
  }

  public static String getFileExtension(File file) {
    return getFileExtension(file.getName());
  }

  public static String getFileExtension(Path path) {
    return getFileExtension(path.getFileName().toString());
  }

  public static String getNameWithoutExtension(String file) {
    return com.google.common.io.Files.getNameWithoutExtension(file);
  }

  public static String getNameWithoutExtension(File file) {
    return getNameWithoutExtension(file.getName());
  }

  public static String getNameWithoutExtension(Path path) {
    return getNameWithoutExtension(path.getFileName().toString());
  }

  public static String getPathWithoutExtension(String path) {
    String ext = getFileExtension(path);
    return !Strings.isNullOrEmpty(ext) ? path.substring(0, path.lastIndexOf("." + ext)) : path;
  }

  public static String getPathWithoutExtension(File file) {
    return getPathWithoutExtension(file.getPath());
  }

  public static String getPathWithoutExtension(Path path) {
    return getPathWithoutExtension(path.toString());
  }

  public static String asPackagePath(@Nullable String filePath) {
    if (filePath == null) return "";
    String str = getPathWithoutExtension(filePath); // remove the extension (if there is one)
    str = PATTERN_FILE_SEPARATORS.matcher(str).replaceAll("."); // replace '/' and '\' with '.'
    if (str.startsWith(".")) str = str.substring(1); // jar files will start with a '/'
    if (str.endsWith("."))
      str = str.substring(0, str.length() - 1); // if the path ended with '/' that will be removed
    return str;
  }

  public static String asPackagePath(File file) {
    return asPackagePath(file.getPath());
  }

  public static String asPackagePath(Path path) {
    return asPackagePath(path.toString());
  }

  public static String asFilePath(@Nullable String packagePath, String separator) {
    return packagePath == null ? "" : packagePath.replace(".", separator);
  }

  public static String getPackageFromFullPath(String file) {
    Matcher matcher = PATTERN_PACKAGE_FROM_PATH.matcher(file);
    return matcher.find() ? matcher.group(1) : null;
  }

  public static String getJarPathFromFullPath(String file) {
    Matcher matcher = PATTERN_JAR_DIR_FROM_PATH.matcher(file);
    return matcher.find() ? matcher.group(1) : null;
  }

  public static FileSystem newFileSystem(Path filePath, ClassLoader parent) throws IOException {
    return FileSystems.newFileSystem(filePath, parent);
  }

  public static FileSystem newFileSystem(File file, ClassLoader parent) throws IOException {
    return FileSystems.newFileSystem(file.toPath(), parent);
  }

  public static FileSystem newFileSystem(String filePath, ClassLoader parent) throws IOException {
    return FileSystems.newFileSystem(Paths.get(filePath), parent);
  }

  public static FileSystem newFileSystem(Path filePath) throws IOException {
    return newFileSystem(filePath, null);
  }

  public static FileSystem newFileSystem(File file) throws IOException {
    return newFileSystem(file, null);
  }

  public static FileSystem newFileSystem(String filePath) throws IOException {
    return newFileSystem(filePath, null);
  }
}
