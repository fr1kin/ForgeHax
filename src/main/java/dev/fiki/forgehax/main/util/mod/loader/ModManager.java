package dev.fiki.forgehax.main.util.mod.loader;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dev.fiki.forgehax.main.util.FileHelper;
import dev.fiki.forgehax.main.util.Immutables;
import dev.fiki.forgehax.main.util.classloader.AbstractClassLoader;
import dev.fiki.forgehax.main.util.classloader.ClassLoaderHelper;
import dev.fiki.forgehax.main.util.classloader.CustomClassLoaders;
import dev.fiki.forgehax.main.util.cmd.ICommand;
import dev.fiki.forgehax.main.util.mod.AbstractMod;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static dev.fiki.forgehax.main.Common.getLogger;

/**
 * Created on 5/16/2017 by fr1kin
 */
public class ModManager extends AbstractClassLoader<AbstractMod> {
  private final Set<Class<? extends AbstractMod>> classes = Sets.newHashSet();

  private final Comparator<AbstractMod> comparator =
      Comparator.comparing(ICommand::getName, String.CASE_INSENSITIVE_ORDER);
  private final Set<AbstractMod> active = Sets.newTreeSet(comparator);

  private Map<Class<? extends AbstractMod>, AbstractMod> CLASS_TO_INSTANCE = Maps.newHashMap();
  private Map<String, AbstractMod> NAME_TO_INSTANCE = Maps.newTreeMap(String::compareToIgnoreCase);

  private volatile Set<AbstractMod> readOnlyActiveMods = ImmutableSortedSet.copyOf(comparator, active);

  private ReadWriteLock lock = new ReentrantReadWriteLock();

  public boolean searchPackage(String packageDir) {
    try {
      return classes.addAll(
          filterClassPaths(getFMLClassLoader(),
              ClassLoaderHelper.getClassesInPackageRecursive(getFMLClassLoader(), packageDir)));
    } catch (IOException | NullPointerException | ClassLoaderHelper.UnknownConnectionTypeException e) {
      getLogger().error("Failed to search package \"{}\"", packageDir);
      getLogger().error(e, e);
    }
    return false;
  }

  public boolean searchPlugin(Path jar, String packageDir) {
    if (!Files.exists(jar)) {
      throw new IllegalArgumentException("path must lead to an existing jar file");
    }
    try {
      getLogger().debug("Loading plugin jar \"{}\"", jar.toAbsolutePath());
      FileSystem fs = FileHelper.newFileSystem(jar);
      ClassLoader classLoader = CustomClassLoaders.newFsClassLoader(getFMLClassLoader(), fs);
      return classes.addAll(filterClassPaths(classLoader, ClassLoaderHelper.getClassPathsInJar(jar, packageDir)));
    } catch (IOException e) {
      getLogger().error("Failed to search plugin jarfile \"{}\" -> \"{}\"",
          jar.toAbsolutePath(), packageDir);
      getLogger().error(e, e);
      return false;
    }
  }

  public boolean searchPluginDirectory(Path directory, String packageDir) {
    if (!Files.exists(directory)) {
      getLogger().warn("plugin directory \"{}\" does not exist!", directory.toAbsolutePath());
      return false;
    }
    if (!Files.isDirectory(directory)) {
      getLogger().warn("\"{}\" is not a directory", directory.toAbsolutePath());
      return false;
    }
    try {
      return Files.list(directory)
          .filter(Files::isRegularFile)
          .filter(path -> FileHelper.getFileExtension(path).equals("jar"))
          .map(path -> searchPlugin(path, Strings.nullToEmpty(packageDir)))
          .filter(Boolean.TRUE::equals)
          .count() > 1;
    } catch (IOException e) {
      getLogger().error("Failed to search plugin directory \"{}\"", directory.toAbsolutePath());
      getLogger().error(e, e);
    }
    return false;
  }

  public boolean searchPluginDirectory(Path directory) {
    return searchPluginDirectory(directory, null);
  }

  private Stream<Class<? extends AbstractMod>> unloadedClasses() {
    return classes.stream()
        .filter(clazz -> !get(clazz).isPresent());
  }

  private Stream<Class<? extends AbstractMod>> loadedClasses() {
    return classes.stream()
        .filter(clazz -> get(clazz).isPresent());
  }

  public Collection<Class<? extends AbstractMod>> getUnloadedClasses() {
    return unloadedClasses().collect(Immutables.toImmutableList());
  }

  public Collection<Class<? extends AbstractMod>> getLoadedClasses() {
    return loadedClasses().collect(Immutables.toImmutableList());
  }

  public Collection<AbstractMod> getMods() {
    return readOnlyActiveMods;
  }

  public Optional<? extends AbstractMod> get(final String modName) {
    lock.readLock().lock();
    try {
      return Optional.ofNullable(NAME_TO_INSTANCE.get(modName));
    } finally {
      lock.readLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends AbstractMod> Optional<T> get(final Class<T> clazz) {
    lock.readLock().lock();
    try {
      return Optional.ofNullable((T) CLASS_TO_INSTANCE.get(clazz));
    } finally {
      lock.readLock().unlock();
    }
  }

  public boolean isModEnabled(Class<? extends AbstractMod> clazz) {
    return get(clazz)
        .map(AbstractMod::isEnabled)
        .orElse(false);
  }

  public void load(Class<? extends AbstractMod> clazz) {
    unloadedClasses()
        .filter(clazz::equals)
        .findFirst()
        .ifPresent(this::_load);
  }

  private boolean addActiveMod(AbstractMod mod) {
    Objects.requireNonNull(mod, "Mod is null!");
    lock.writeLock().lock();
    try {
      if (active.add(mod)) {
        CLASS_TO_INSTANCE.put(mod.getClass(), mod);
        NAME_TO_INSTANCE.put(mod.getName(), mod);
        readOnlyActiveMods = ImmutableSortedSet.copyOf(comparator, active);
        return true;
      }
    } finally {
      lock.writeLock().unlock();
    }
    return false;
  }

  private boolean removeActiveMod(AbstractMod mod) {
    Objects.requireNonNull(mod, "Mod is null!");
    lock.writeLock().lock();
    try {
      if (active.remove(mod)) {
        CLASS_TO_INSTANCE.remove(mod.getClass());
        NAME_TO_INSTANCE.remove(mod.getName());
        readOnlyActiveMods = ImmutableSortedSet.copyOf(comparator, active);
        return true;
      }
    } finally {
      lock.writeLock().unlock();
    }
    return false;
  }

  private void _load(Class<? extends AbstractMod> clazz) {
    if (addActiveMod(loadClass(clazz))) {
      getLogger().debug("Loading class {}", clazz.getSimpleName());
    } else {
      getLogger().warn("Failed to add mod {}. Possibly already exists?", clazz.getSimpleName());
    }
  }

  public void loadAll() {
    unloadedClasses().forEach(this::_load);
  }

  public void unload(AbstractMod mod) {
    if (removeActiveMod(mod)) {
      mod.unload();
    }
  }

  public void unloadAll() {
    active.forEach(this::unload);
  }

  public void refresh() {
    forEach(AbstractMod::unload);
    forEach(AbstractMod::load);
  }

  public void forEach(final Consumer<AbstractMod> consumer) {
    active.forEach(consumer);
  }

  @Nullable
  @Override
  public Class<AbstractMod> getInheritedClass() {
    return AbstractMod.class;
  }

  @Nullable
  @Override
  public Class<? extends Annotation> getAnnotationClass() {
    return RegisterMod.class;
  }
}
