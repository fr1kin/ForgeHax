package dev.fiki.forgehax.main.util.mod.loader;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import dev.fiki.forgehax.main.util.mod.BaseMod;
import dev.fiki.forgehax.main.util.FileHelper;
import dev.fiki.forgehax.main.util.Immutables;
import dev.fiki.forgehax.main.util.classloader.AbstractClassLoader;
import dev.fiki.forgehax.main.util.classloader.ClassLoaderHelper;
import dev.fiki.forgehax.main.util.classloader.CustomClassLoaders;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import static dev.fiki.forgehax.main.Common.*;

/**
 * Created on 5/16/2017 by fr1kin
 */
public class ModManager extends AbstractClassLoader<BaseMod> {
  private final Set<Class<? extends BaseMod>> classes = Sets.newHashSet();
  private final Set<BaseMod> active =
      Sets.newTreeSet(Comparator.comparing(BaseMod::getModName, String.CASE_INSENSITIVE_ORDER));

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

  private Stream<Class<? extends BaseMod>> unloadedClasses() {
    return classes.stream()
        .filter(clazz -> active.stream().noneMatch(mod -> mod.getClass().equals(clazz)));
  }

  private Stream<Class<? extends BaseMod>> loadedClasses() {
    return classes.stream()
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
    return active.stream()
        .filter(mod -> Objects.equals(clazz, mod.getClass()))
        .map(mod -> (T) mod)
        .findFirst();
  }

  public void load(Class<? extends BaseMod> clazz) {
    unloadedClasses().filter(clazz::equals).findFirst().ifPresent(this::_load);
  }

  private void _load(Class<? extends BaseMod> clazz) {
    if (active.add(loadClass(clazz))) {
      getLogger().debug("Loading class {}", clazz.getSimpleName());
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
