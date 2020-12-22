package dev.fiki.forgehax.api.classloader;

import com.google.common.collect.Lists;
import dev.fiki.forgehax.api.Streamables;
import dev.fiki.forgehax.main.ForgeHax;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import org.objectweb.asm.Type;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static dev.fiki.forgehax.api.FileHelper.*;

@Log4j2
public class ClassLoaderHelper {
  private static Class<?> MODJAR_URLCONNECTION_CLASS;

  @SneakyThrows
  public static Class<?> getModjarUrlconnectionClass() {
    if (MODJAR_URLCONNECTION_CLASS == null) {
      MODJAR_URLCONNECTION_CLASS = Class.forName("net.minecraftforge.fml.loading.ModJarURLHandler$ModJarURLConnection");
    }
    return MODJAR_URLCONNECTION_CLASS;
  }

  public static String getForgeHaxPath() {
    return Type.getInternalName(ForgeHax.class) + ".class";
  }

  private static void collectPathRecursively(Path directory, List<Path> collected)
      throws IOException {
    if (directory != null
        && Files.exists(directory)
        && Files.isDirectory(directory)) {
      for (Path path : Files.list(directory).collect(Collectors.toList())) {
        collected.add(path);
        if (Files.isDirectory(path)) {
          collectPathRecursively(path, collected);
        }
      }
    }
  }

  public static List<Path> getJarPathsInDirectory(final Path directory, boolean recursive) throws IOException {
    final List<Path> results = Lists.newArrayList();
    collectPathRecursively(directory, results);
    return results.stream()
        .filter(path -> "jar".equalsIgnoreCase(getFileExtension(path)))
        .collect(Collectors.toList());
  }

  public static List<Path> getJarPathsInDirectory(File directory, boolean recursive) throws IOException {
    return getJarPathsInDirectory(directory.toPath(), recursive);
  }

  public static List<Path> getJarPathsInDirectory(String directory, boolean recursive) throws IOException {
    return getJarPathsInDirectory(Paths.get(directory), recursive);
  }

  public static List<Path> getJarPathsInDirectory(String directory) throws IOException {
    boolean recursive = directory.endsWith("/*") || directory.endsWith("\\*");
    return getJarPathsInDirectory(
        Paths.get(recursive ? directory.substring(0, directory.length() - 2) : directory),
        recursive);
  }

  /**
   * Generates a list of all files inside the directory (recursively).
   *
   * @param directory root directory to initiate scan
   * @param recursive if the scan should look into sub directories
   * @return All files inside the directory that have the .class extension
   */
  public static List<Path> getClassPathsInDirectory(Path directory, boolean recursive) throws IOException {
    final List<Path> results = Lists.newArrayList();
    collectPathRecursively(directory, results);
    return results.stream()
        .filter(path -> "class".equalsIgnoreCase(getFileExtension(path)))
        .collect(Collectors.toList());
  }

  public static List<Path> getClassPathsInDirectory(File directory, boolean recursive) throws IOException {
    return getClassPathsInDirectory(directory.toPath(), recursive);
  }

  public static List<Path> getClassPathsInDirectory(String directory, boolean recursive) throws IOException {
    return getClassPathsInDirectory(Paths.get(directory), recursive);
  }

  public static List<Path> getClassPathsInDirectory(String directory) throws IOException {
    boolean recursive = directory.endsWith("/*") || directory.endsWith("\\*");
    return getClassPathsInDirectory(
        Paths.get(recursive ? directory.substring(0, directory.length() - 2) : directory),
        recursive);
  }

  /**
   * Generates a list of all the paths that have the file extension '.class'
   *
   * @param jarFile    jar file
   * @param packageDir path to the package
   * @param recursive  if the scan should look into sub directories
   * @return a list of class paths
   * @throws IOException if there is an issue opening/reading the files
   */
  public static List<Path> getClassPathsInJar(JarFile jarFile, String packageDir, boolean recursive)
      throws IOException {
    Objects.requireNonNull(jarFile);
    Objects.requireNonNull(packageDir);

    // open new file system to the jar file
    final FileSystem fs = newFileSystem(jarFile.getName());
    final Path root = fs.getRootDirectories().iterator().next();
    final Path packagePath = root.resolve(packageDir);

    return Streamables.enumerationStream(jarFile.entries())
        .map(entry -> root.resolve(entry.getName()))
        .filter(path -> "class".equalsIgnoreCase(getFileExtension(path)))
        .filter(path -> recursive
            // name count = directories, +1 for the class file name
            || path.getNameCount() == packagePath.getNameCount() + 1)
        .filter(path -> path.toString().startsWith(path.getFileSystem().getSeparator() + packageDir)
            // 2 = root (first) '/' + suffix '/'
            && path.toString().length() > (packageDir.length() + 2))
        .collect(Collectors.toList());
  }

  public static List<Path> getClassPathsInJar(File file, String packagePath, boolean recursive)
      throws IOException {
    Objects.requireNonNull(file);
    return getClassPathsInJar(new JarFile(file), packagePath, recursive);
  }

  public static List<Path> getClassPathsInJar(Path path, String packagePath, boolean recursive)
      throws IOException {
    Objects.requireNonNull(path);
    return getClassPathsInJar(path.toFile(), packagePath, recursive);
  }

  public static List<Path> getClassPathsInJar(JarFile jarFile, String packageDir)
      throws IOException {
    boolean recursive = packageDir.endsWith(".*");
    return getClassPathsInJar(
        jarFile,
        recursive ? packageDir.substring(0, packageDir.length() - 2) : packageDir,
        recursive);
  }

  public static List<Path> getClassPathsInJar(File file, String packagePath) throws IOException {
    Objects.requireNonNull(file);
    return getClassPathsInJar(new JarFile(file.getPath()), packagePath);
  }

  public static List<Path> getClassPathsInJar(Path path, String packagePath) throws IOException {
    Objects.requireNonNull(path);
    return getClassPathsInJar(path.toFile(), packagePath);
  }

  /**
   * Will attempt to find every class inside the package recursively.
   *
   * @param classLoader class loader to get the package resource from
   * @param packageIn   name of package to search
   * @param recursive   if the scan should look into sub directories
   * @return list of all the classes found
   * @throws IOException
   */
  public static List<Path> getClassesInPackage(final ClassLoader classLoader,
      final String packageIn, final boolean recursive)
      throws IOException, UnknownConnectionTypeException {
    Objects.requireNonNull(packageIn, "Package is missing");
    Objects.requireNonNull(classLoader, "ClassLoader is null");

    // package directory
    final String jarPackageDir = asFilePath(packageIn, "/");
    final String packageFileDir = asFilePath(packageIn, File.separator);

    final String mainPath = getForgeHaxPath();
    final URL url = classLoader.getResource(mainPath);

    Objects.requireNonNull(url, "ForgeHax url resource could not be found!");

    final URLConnection connection = url.openConnection();

    log.debug("Connecting to jar using {}", connection.getClass());

    if (isFMLConnection(connection)) {
      final ModFile modFile = getModFileInModJar(connection);

      final Path jar = modFile.getFilePath();
      if (Files.isRegularFile(jar)) {
        return getClassPathsInJar(new JarFile(jar.toFile()), jarPackageDir, recursive);
      } else {
        final Path buildPath = modFile.findResource(packageFileDir);
        // this is kind of gay but i guess that's just how it has to be
        final Path buildRootPath = Paths.get(buildPath.toString().substring(0, buildPath.toString().indexOf(packageFileDir)));

        return getClassPathsInDirectory(buildPath, recursive).stream()
            .map(buildRootPath::relativize)
            .collect(Collectors.toList());
      }
    } else {
      // get the path to the jar/folder containing the classes
      String path = URLDecoder.decode(url.getPath(), "UTF-8")
          .replace('\\', '/'); // get path and covert backslashes to forward slashes

      // remove the initial '/' or 'file:/' appended to the path
      path = path.substring(path.indexOf('/') + 1);

      if (!System.getProperty("os.name").toLowerCase().contains("win")) {
        // add the / back to unix based systems
        path = "/" + path;
      }

      // remove ForgeHax.class classpath
      path = path.substring(0, path.indexOf(mainPath));
      path += jarPackageDir;

      // the root directory to the jar/folder containing the classes
      final String rootDir = path.substring(0, path.indexOf(jarPackageDir));

      if (connection instanceof FileURLConnection) { // FileURLConnection doesn't seem to be used
        final Path root = Paths.get(rootDir).normalize();
        return getClassPathsInDirectory(path, recursive)
            .stream()
            .map(root::relativize)
            .collect(Collectors.toList());
      } else if (connection instanceof JarURLConnection) {
        return getClassPathsInJar(((JarURLConnection) connection).getJarFile(), jarPackageDir, recursive);
      }
    }

    throw new UnknownConnectionTypeException(connection.getClass());
  }

  public static List<Path> getClassesInPackage(final ClassLoader classLoader, String packageDir)
      throws IOException, UnknownConnectionTypeException {
    return getClassesInPackage(classLoader, packageDir, false);
  }

  public static List<Path> getClassesInPackageRecursive(final ClassLoader classLoader, String packageDir)
      throws IOException, UnknownConnectionTypeException {
    return getClassesInPackage(classLoader, packageDir, true);
  }

  public static List<Class<?>> getLoadedClasses(
      final ClassLoader classLoader, Collection<Path> paths) {
    Objects.requireNonNull(classLoader);
    Objects.requireNonNull(paths);
    return paths.stream()
        .map(path -> {
          try {
            return Class.forName(asPackagePath(path), false, classLoader);
          } catch (ClassNotFoundException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  public static boolean isFMLConnection(URLConnection connection) {
    return connection.getClass().equals(getModjarUrlconnectionClass());
  }

  @SneakyThrows
  public static ModFile getModFileInModJar(URLConnection connection) {
    // getURL() is overwritten in latest forge, so we must get the url field from the URLConnection class
    // via reflection
    final Field urlField = URLConnection.class.getDeclaredField("url");
    urlField.setAccessible(true);
    final String hostname = ((URL) urlField.get(connection)).getHost();
    return Objects.requireNonNull(FMLLoader.getLoadingModList().getModFileById(hostname),
        "Failed to find ForgeHax mod file! (" + hostname + ")").getFile();
  }

  public static class UnknownConnectionTypeException extends Exception {
    public UnknownConnectionTypeException(Class<? extends URLConnection> type) {
      super(type.toString());
    }
  }
}
