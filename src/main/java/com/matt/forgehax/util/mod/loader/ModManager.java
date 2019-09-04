package com.matt.forgehax.util.mod.loader;

import static com.matt.forgehax.Helper.getLog;

import com.google.common.collect.Sets;
import com.matt.forgehax.Globals;
import com.matt.forgehax.util.ArrayHelper;
import com.matt.forgehax.util.FileHelper;
import com.matt.forgehax.util.Immutables;
import com.matt.forgehax.util.classloader.AbstractClassLoader;
import com.matt.forgehax.util.classloader.ClassLoaderHelper;
import com.matt.forgehax.util.classloader.CustomClassLoaders;
import com.matt.forgehax.util.mod.BaseMod;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * Created on 5/16/2017 by fr1kin
 */
public class ModManager extends AbstractClassLoader<BaseMod> implements Globals {
  
  private static final ModManager INSTANCE = new ModManager();
  
  public static ModManager getInstance() {
    return INSTANCE;
  }
  
  //
  //
  //
  
  private final Set<Class<? extends BaseMod>> classes = Sets.newHashSet();
  private final Set<BaseMod> active =
    Sets.newTreeSet(Comparator.comparing(BaseMod::getModName, String.CASE_INSENSITIVE_ORDER));
  
  public boolean searchPackage(String packageDir) {
    try {
      return classes.addAll(
        filterClassPaths(
          getFMLClassLoader(),
          ClassLoaderHelper.getClassPathsInPackage(getFMLClassLoader(), packageDir)));
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }
  
  public boolean searchPlugin(Path jar, String packageDir) {
    if (!Files.exists(jar)) {
      throw new IllegalArgumentException("path must lead to an existing jar file");
    }
    try {
      FileSystem fs = FileHelper.newFileSystem(jar);
      ClassLoader classLoader = CustomClassLoaders.newFsClassLoader(getFMLClassLoader(), fs);
      return classes.addAll(
        filterClassPaths(classLoader, ClassLoaderHelper.getClassPathsInJar(jar, packageDir)));
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }
  
  public boolean searchPluginDirectory(Path directory, String... packageDir) {
    if (packageDir.length > 1) {
      throw new IllegalArgumentException("Path should be contained in first array index");
    }
    if (!Files.exists(directory)) {
      getLog().warn("plugin directory '" + directory.toString() + "' does not exist");
      return false;
    }
    if (!Files.isDirectory(directory)) {
      getLog().warn("path '" + directory.toString() + "' is not a directory");
      return false;
    }
    try {
      return Files.list(directory)
        .filter(Files::isRegularFile)
        .filter(path -> FileHelper.getFileExtension(path).equals("jar"))
        .anyMatch(path -> searchPlugin(path, ArrayHelper.getOrDefault(packageDir, 0, "")));
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }
  
  public boolean searchPluginDirectory(String directory, String... packageDir) {
    return searchPluginDirectory(Paths.get(directory), packageDir);
  }
  
  private Stream<Class<? extends BaseMod>> unloadedClasses() {
    return classes
      .stream()
      .filter(clazz -> active.stream().noneMatch(mod -> mod.getClass().equals(clazz)));
  }
  
  private Stream<Class<? extends BaseMod>> loadedClasses() {
    return classes
      .stream()
      .filter(clazz -> active.stream().anyMatch(mod -> mod.getClass().equals(clazz)));
  }
  
  public Collection<Class<? extends BaseMod>> getUnloadedClasses() {
    return unloadedClasses().collect(Immutables.toImmutableList());
  }
  
  public Collection<Class<? extends BaseMod>> getLoadedClasses() {
    return loadedClasses().collect(Immutables.toImmutableList());
  }
  
  public Collection<BaseMod> getMods() {
    return Collections.unmodifiableCollection(active);
  }
  
  public Optional<? extends BaseMod> get(final String modName) {
    return active.stream().filter(mod -> mod.getModName().equalsIgnoreCase(modName)).findFirst();
  }
  
  @SuppressWarnings("unchecked")
  public <T extends BaseMod> Optional<T> get(final Class<T> clazz) {
    return active
      .stream()
      .filter(mod -> Objects.equals(clazz, mod.getClass()))
      .map(mod -> (T) mod)
      .findFirst();
  }
  
  public void load(Class<? extends BaseMod> clazz) {
    unloadedClasses().filter(clazz::equals).findFirst().ifPresent(this::_load);
  }
  
  private void _load(Class<? extends BaseMod> clazz) {
    if (active.add(loadClass(clazz))) {
      getLog().info("Loaded mod " + clazz.getSimpleName());
    }
  }
  
  public void loadAll() {
    unloadedClasses().forEach(this::_load);
  }
  
  public void unload(BaseMod mod) {
    if (active.remove(mod)) {
      mod.unload();
    }
  }
  
  public void unloadAll() {
    active.forEach(this::unload);
  }
  
  public void refresh() {
    forEach(BaseMod::unload);
    forEach(BaseMod::load);
  }
  
  public void forEach(final Consumer<BaseMod> consumer) {
    active.forEach(consumer);
  }
  
  @Nullable
  @Override
  public Class<BaseMod> getInheritedClass() {
    return BaseMod.class;
  }
  
  @Nullable
  @Override
  public Class<? extends Annotation> getAnnotationClass() {
    return RegisterMod.class;
  }
}
