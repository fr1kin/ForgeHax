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
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static com.matt.forgehax.util.FileHelper.*;

public class ClassLoaderHelper {
    private static void getClassPathsInDirectory(final Path directory, final List<Path> found, final boolean recursive) {
        Optional.ofNullable(directory)
                .filter(Files::exists)
                .filter(Files::isDirectory)
                .ifPresent(dir -> {
                    try {
                        Files.list(dir).forEach(path -> {
                            if(Files.isDirectory(path)) {
                                if(recursive) getClassPathsInDirectory(path, found, true); // will always be true;
                            }
                            else if(getFileExtension(path).equals("class"))
                                found.add(path);
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Generates a list of all files inside the directory (recursively).
     * @param directory root directory to initiate scan
     * @param recursive if the scan should look into sub directories
     * @return All files inside the directory that have the .class extension
     */
    public static List<Path> getClassPathsInDirectory(Path directory, boolean recursive) {
        List<Path> found = Lists.newArrayList();
        getClassPathsInDirectory(directory, found, recursive);
        return found;
    }
    public static List<Path> getClassPathsInDirectory(File directory, boolean recursive) {
        return getClassPathsInDirectory(directory.toPath(), recursive);
    }
    public static List<Path> getClassPathsInDirectory(String directory, boolean recursive) {
        return getClassPathsInDirectory(Paths.get(directory), recursive);
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
        FileSystem fs = FileSystems.newFileSystem(Paths.get(jarFile.getName()), null);
        final Path root = fs.getPath("/");
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
                        if(connection instanceof FileURLConnection) {
                            results.addAll(getClassPathsInDirectory(
                                    URLDecoder.decode(url.getPath(), "UTF-8").substring(1), // substring(1) will remove '/'
                                    recursive
                            ));
                        }
                        else if(connection instanceof JarURLConnection) {
                            results.addAll(getClassPathsInJar(
                                    ((JarURLConnection)connection).getJarFile(),
                                    asFilePath(getPackageFromFullPath(URLDecoder.decode(url.getPath(), "UTF-8").substring(6))), // substring(6) will remove 'file:/'
                                    recursive
                            ));
                        }
                        else if(connection instanceof sun.net.www.protocol.jar.JarURLConnection) {
                            results.addAll(getClassPathsInJar(
                                    ((sun.net.www.protocol.jar.JarURLConnection)connection).getJarFile(),
                                    asFilePath(getPackageFromFullPath(URLDecoder.decode(url.getPath(), "UTF-8").substring(6))), // substring(6) will remove 'file:/'
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
