package dev.fiki.forgehax.main.util.mod.loader;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import dev.fiki.forgehax.main.util.cmd.ICommand;
import dev.fiki.forgehax.main.util.mod.AbstractMod;
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
public class ModManager extends AbstractClassLoader<AbstractMod> {
  private final Set<Class<? extends AbstractMod>> classes = Sets.newHashSet();
  private final Set<AbstractMod> active =
      Sets.newTreeSet(Comparator.comparing(ICommand::getName, String.CASE_INSENSITIVE_ORDER));

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
        .filter(clazz -> active.stream().noneMatch(mod -> mod.getClass().equals(clazz)));
  }

  private Stream<Class<? extends AbstractMod>> loadedClasses() {
    return classes.stream()
        .filter(clazz -> active.stream().anyMatch(mod -> mod.getClass().equals(clazz)));
  }

  public Collection<Class<? extends AbstractMod>> getUnloadedClasses() {
    return unloadedClasses().collect(Immutables.toImmutableList());
  }

  public Collection<Class<? extends AbstractMod>> getLoadedClasses() {
    return loadedClasses().collect(Immutables.toImmutableList());
  }

  public Collection<AbstractMod> getMods() {
    return Collections.unmodifiableCollection(active);
  }

  public Optional<? extends AbstractMod> get(final String modName) {
    return active.stream().filter(mod -> mod.getName().equalsIgnoreCase(modName)).findFirst();
  }

  @SuppressWarnings("unchecked")
  public <T extends AbstractMod> Optional<T> get(final Class<T> clazz) {
    return active.stream()
        .filter(mod -> Objects.equals(clazz, mod.getClass()))
        .map(mod -> (T) mod)
        .findFirst();
  }

  public void load(Class<? extends AbstractMod> clazz) {
    unloadedClasses().filter(clazz::equals).findFirst().ifPresent(this::_load);
  }

  private void _load(Class<? extends AbstractMod> clazz) {
    AbstractMod mod = loadClass(clazz);
    if (mod != null && active.add(mod)) {
      getLogger().debug("Loading class {}", clazz.getSimpleName());
    } else {
      getLogger().warn("Mod {} is null!", clazz.getSimpleName());
    }
  }

  public void loadAll() {
    unloadedClasses().forEach(this::_load);
  }

  public void unload(AbstractMod mod) {
    if (active.remove(mod)) {
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
