package com.matt.forgehax.util.classloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.matt.forgehax.util.FileHelper.asFilePath;

/**
 * Created on 2/16/2018 by fr1kin
 */
public class CustomClassLoaders {
    public static ClassLoader newPathClassLoader(ClassLoader parent, FileSystem fs) throws RuntimeException {
        try {
            return new PathClassLoader(parent, fs);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static class PathClassLoader extends URLClassLoader {
        private final Path path;

        public PathClassLoader(ClassLoader parent, Path path) throws MalformedURLException {
            super(new URL[] {path.toUri().toURL()}, parent);
            this.path = path;
        }
        public PathClassLoader(ClassLoader parent, FileSystem fileSystem) throws MalformedURLException {
            this(parent, fileSystem.getPath("/"));
        }

        public FileSystem getFileSystem() {
            return path.getFileSystem();
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                Path location = path.resolve(asFilePath(name).concat(".class"));
                if (Files.exists(location)) {
                    byte[] classData = Files.readAllBytes(location);
                    Class<?> clazz = defineClass(name, classData, 0, classData.length);
                    if (clazz != null) return clazz;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
