package com.matt.forgehax.util.mod.loader;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.reflect.ClassPath;
import com.matt.forgehax.util.mod.BaseMod;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.matt.forgehax.Helper.getFileManager;
import static com.matt.forgehax.Helper.getLog;

/**
 * Created on 8/12/2017 by fr1kin
 */
public class PluginLoader {
    private static final File PLUGIN_DIR = getFileManager().getFileInBaseDirectory("plugins");

    static {
        PLUGIN_DIR.mkdirs();
    }

    public static List<File> getJars() {
        List<File> jars = Lists.newArrayList();
        try {
            for (File file : PLUGIN_DIR.listFiles()) {
                if(file.isFile()
                        && Files.getFileExtension(file.getName()).equalsIgnoreCase("jar"))
                    jars.add(file);
            }
        } catch (Throwable t) {}
        return jars;
    }

    private static Collection<ClassPath.ClassInfo> getPluginClassInfo(File jar) {
        try {
            URLClassLoader loader = new URLClassLoader(new URL[] {jar.toURI().toURL()}, PluginLoader.class.getClassLoader());
            ClassPath path = ClassPath.from(loader);
            return path.getTopLevelClassesRecursive("com.forgehax");
        } catch (Throwable t) {
            return Collections.emptySet();
        }
    }

    public static List<Class<?>> getAllClasses(File resource, String pkgname) {
        List<Class<?>> classes = Lists.newArrayList();

        try {
            URL url = resource.toURI().toURL();
            //Turn package name to relative path to jar file
            String relPath = pkgname.replace('.', '/');
            String resPath = url.getPath();
            String jarPath = resPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
            // jarPath = jarPath.replace(" ", "\\ ");
            JarFile jarFile;

            URLClassLoader cl = URLClassLoader.newInstance(new URL[]{url}, Thread.currentThread().getContextClassLoader());

            try {
                jarFile = new JarFile(jarPath);
            } catch (IOException e) {
                throw new RuntimeException("Unexpected IOException reading JAR File '" + jarPath + "'", e);
            }

            //get contents of jar file and iterate through them
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                //Get content name from jar file
                String entryName = entry.getName();
                String className = null;

                //If content is a class save class name.
                if (entryName.endsWith(".class") && entryName.startsWith(relPath)
                        && entryName.length() > (relPath.length() + "/".length())) {
                    className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
                }

                //If content is a class add class to List
                if (className != null) {
                    try {
                        classes.add(cl.loadClass(className));
                    } catch (Throwable t) {
                        getLog().error("Failed to load class: " + t.getMessage());
                    }
                }
            }
        } catch (Throwable t) {
            getLog().error(t.getMessage());
            t.printStackTrace();
        }

        return classes;
    }

    @SuppressWarnings("unchecked")
    public static Collection<Class<? extends BaseMod>> getPluginClasses() {
        final Collection<Class<? extends BaseMod>> classes = Lists.newArrayList();
        getJars().forEach(file -> {
            getLog().info(String.format("Found jar \"%s\"", file.getName()));
            getPluginClassInfo(file).forEach(info -> {
                Class<? extends BaseMod> clazz = (Class<? extends BaseMod>) info.load();
                if(clazz.isAnnotationPresent(RegisterMod.class)) classes.add(clazz);
            });
        });
        return Collections.unmodifiableCollection(classes);
    }
}
