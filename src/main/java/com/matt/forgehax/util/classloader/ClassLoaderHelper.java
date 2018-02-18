package com.matt.forgehax.util.classloader;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.Streamables;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static com.matt.forgehax.util.FileHelper.*;

public class ClassLoaderHelper {
    private static void searchDirectory(final Path directory, final List<Path> results, final BiFunction<Path, List<Path>, Boolean> function) {
        Optional.ofNullable(directory)
                .filter(Files::exists)
                .filter(Files::isDirectory)
                .ifPresent(dir -> {
                    try {
                        Files.list(dir).forEach(path -> {
                            if(function.apply(path, results)) searchDirectory(path, results, function);
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public static List<Path> getJarPathsInDirectory(Path directory, boolean recursive) {
        List<Path> results = Lists.newArrayList();
        searchDirectory(directory, results, (path, paths) -> {
            if(Files.isDirectory(path))
                return recursive;
            else if(getFileExtension(path).equals("jar"))
                paths.add(path);
            return false;
        });
        return results;
    }
    public static List<Path> getJarPathsInDirectory(File directory, boolean recursive) {
        return getJarPathsInDirectory(directory.toPath(), recursive);
    }
    public static List<Path> getJarPathsInDirectory(String directory, boolean recursive) {
        return getJarPathsInDirectory(Paths.get(directory), recursive);
    }
    public static List<Path> getJarPathsInDirectory(String directory) {
        boolean recursive = directory.endsWith("/*") || directory.endsWith("\\*");
        return getJarPathsInDirectory(Paths.get(recursive ? directory.substring(0, directory.length() - 2) : directory), recursive);
    }

    /**
     * Generates a list of all files inside the directory (recursively).
     * @param directory root directory to initiate scan
     * @param recursive if the scan should look into sub directories
     * @return All files inside the directory that have the .class extension
     */
    public static List<Path> getClassPathsInDirectory(Path directory, boolean recursive) {
        List<Path> results = Lists.newArrayList();
        searchDirectory(directory, results, (path, paths) -> {
            if(Files.isDirectory(path))
                return recursive;
            else if(getFileExtension(path).equals("class"))
                paths.add(path);
            return false;
        });
        return results;
    }
    public static List<Path> getClassPathsInDirectory(File directory, boolean recursive) {
        return getClassPathsInDirectory(directory.toPath(), recursive);
    }
    public static List<Path> getClassPathsInDirectory(String directory, boolean recursive) {
        return getClassPathsInDirectory(Paths.get(directory), recursive);
    }
    public static List<Path> getClassPathsInDirectory(String directory) {
        boolean recursive = directory.endsWith("/*") || directory.endsWith("\\*");
        return getClassPathsInDirectory(Paths.get(recursive ? directory.substring(0, directory.length() - 2) : directory), recursive);
    }

    /**
     * Generates a list of all the paths that have the file extension '.class'
     * @param jarFile jar file
     * @param packageDir path to the package
     * @param recursive if the scan should look into sub directories
     * @return a list of class paths
     * @throws IOException if there is an issue opening/reading the files
     */
    public static List<Path> getClassPathsInJar(JarFile jarFile, String packageDir, boolean recursive) throws IOException {
        Objects.requireNonNull(jarFile);
        Objects.requireNonNull(packageDir);

        // open new file system to the jar file
        final Path root = newFileSystem(jarFile.getName()).getPath("/");
        final Path packagePath = root.resolve(packageDir);

        return Streamables.enumerationStream(jarFile.entries())
                .map(entry -> root.resolve(entry.getName()))
                .filter(path -> getFileExtension(path).equals("class"))
                .filter(path -> recursive || path.getNameCount() == packagePath.getNameCount() + 1) // name count = directories, +1 for the class file name
                .filter(path -> path.toString().startsWith("/" + packageDir)
                        && path.toString().length() > (packageDir.length() + 2)) // 2 = root (first) '/' + suffix '/'
                .collect(Collectors.toList());
    }
    public static List<Path> getClassPathsInJar(File file, String packagePath, boolean recursive) throws IOException {
        Objects.requireNonNull(file);
        return getClassPathsInJar(new JarFile(file.getPath()), packagePath, recursive);
    }
    public static List<Path> getClassPathsInJar(JarFile jarFile, String packageDir) throws IOException {
        boolean recursive = packageDir.endsWith(".*");
        return getClassPathsInJar(jarFile, recursive ? packageDir.substring(0, packageDir.length() - 2) : packageDir, recursive);
    }
    public static List<Path> getClassPathsInJar(File file, String packagePath) throws IOException {
        Objects.requireNonNull(file);
        return getClassPathsInJar(new JarFile(file.getPath()), packagePath);
    }

    /**
     * Will attempt to find every class inside the package recursively.
     * @param classLoader class loader to get the package resource from
     * @param packageDir name of package to search
     * @param recursive if the scan should look into sub directories
     * @return list of all the classes found
     * @throws IOException
     */
    public static List<Path> getClassPathsInPackage(final ClassLoader classLoader, String packageDir, final boolean recursive) throws IOException {
        Objects.requireNonNull(packageDir);
        Objects.requireNonNull(classLoader);

        List<Path> results = Lists.newArrayList();

        Enumeration<URL> inside = classLoader.getResources(asFilePath(packageDir));
        Streamables.enumerationStream(inside)
                .forEach(url -> {
                    URLConnection connection;
                    try {
                        connection = url.openConnection();
                        String path = URLDecoder.decode(url.getPath(), "UTF-8");
                        if(connection instanceof FileURLConnection) {
                            results.addAll(getClassPathsInDirectory(
                                    path.substring(1), // substring(1) will remove '/'
                                    recursive
                            ));
                        }
                        else if(connection instanceof JarURLConnection) {
                            results.addAll(getClassPathsInJar(
                                    ((JarURLConnection)connection).getJarFile(),
                                    asFilePath(getPackageFromFullPath(path.substring(6))), // substring(6) will remove 'file:/'
                                    recursive
                            ));
                        }
                        else if(connection instanceof sun.net.www.protocol.jar.JarURLConnection) {
                            results.addAll(getClassPathsInJar(
                                    ((sun.net.www.protocol.jar.JarURLConnection)connection).getJarFile(),
                                    asFilePath(getPackageFromFullPath(path.substring(6))), // substring(6) will remove 'file:/'
                                    recursive
                            ));
                        }
                        else throw new UnknownConnectionType();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        return results;
    }
    public static List<Path> getClassPathsInPackage(final ClassLoader classLoader, String packageDir) throws IOException {
        boolean recursive = packageDir.endsWith(".*");
        return getClassPathsInPackage(classLoader, recursive ? packageDir.substring(0, packageDir.length() - 2) : packageDir, recursive);
    }

    public static List<Class<?>> getLoadedClasses(final ClassLoader classLoader, Collection<Path> paths) {
        Objects.requireNonNull(classLoader);
        Objects.requireNonNull(paths);
        return paths.stream()
                .map(path -> {
                    try {
                        return Class.forName(asPackagePath(path.toString()), false, classLoader);
                    } catch (ClassNotFoundException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static List<Class<?>> getLoadedClassesInPackage(final ClassLoader classLoader, String packageDir, boolean recursive) throws IOException {
        return getLoadedClasses(classLoader, getClassPathsInPackage(classLoader, packageDir, recursive));
    }
    public static List<Class<?>> getLoadedClassesInPackage(final ClassLoader classLoader, String packageDir) throws IOException {
        return getLoadedClasses(classLoader, getClassPathsInPackage(classLoader, packageDir));
    }

    public static List<Class<?>> getLoadedClassesInJar(final ClassLoader classLoader, JarFile jarFile, String packageDir, boolean recursive) throws IOException {
        return getLoadedClasses(classLoader, getClassPathsInJar(jarFile, packageDir, recursive));
    }
    public static List<Class<?>> getLoadedClassesInJar(final ClassLoader classLoader, File file, String packageDir, boolean recursive) throws IOException {
        return getLoadedClasses(classLoader, getClassPathsInJar(new JarFile(file.getPath()), packageDir, recursive));
    }
    public static List<Class<?>> getLoadedClassesInJar(final ClassLoader classLoader, JarFile jarFile, String packageDir) throws IOException {
        return getLoadedClasses(classLoader, getClassPathsInJar(jarFile, packageDir));
    }
    public static List<Class<?>> getLoadedClassesInJar(final ClassLoader classLoader, File file, String packageDir) throws IOException {
        return getLoadedClasses(classLoader, getClassPathsInJar(new JarFile(file.getPath()), packageDir));
    }

    public static class UnknownConnectionType extends Exception {}
}
