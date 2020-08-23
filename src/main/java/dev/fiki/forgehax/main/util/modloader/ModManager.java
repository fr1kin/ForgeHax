package dev.fiki.forgehax.main.util.modloader;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import dev.fiki.forgehax.main.util.FileHelper;
import dev.fiki.forgehax.main.util.classloader.AbstractClassLoader;
import dev.fiki.forgehax.main.util.classloader.ClassLoaderHelper;
import dev.fiki.forgehax.main.util.classloader.CustomClassLoaders;
import dev.fiki.forgehax.main.util.mod.AbstractMod;
import dev.fiki.forgehax.main.util.mod.CommandMod;
import dev.fiki.forgehax.main.util.modloader.di.DependencyInjector;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static dev.fiki.forgehax.main.Common.getLogger;

/**
 * Created on 5/16/2017 by fr1kin
 */
@RequiredArgsConstructor
public class ModManager extends AbstractClassLoader<AbstractMod> {
  private final DependencyInjector di;
  private final List<AbstractMod> mods = Lists.newArrayList();

  public boolean searchPackage(String packageDir) {
    try {
      filterClassPaths(
          getFMLClassLoader(),
          ClassLoaderHelper.getClassesInPackageRecursive(getFMLClassLoader(), packageDir)
      ).forEach(di::module);
      return true;
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

      filterClassPaths(classLoader, ClassLoaderHelper.getClassPathsInJar(jar, packageDir))
          .forEach(di::module);

      return true;
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

  public Stream<AbstractMod> getMods() {
    return mods.stream();
  }

  public void loadMods() {
    di.getDependenciesAnnotatedWith(RegisterMod.class)
        .forEach(dep -> {
          try {
            dep.getInstance(di);
          } catch (Throwable t) {
            getLogger().warn("Failed to load mod {}", dep.getTargetClass().getSimpleName());
            getLogger().warn(t, t);
          }
        });
  }

  public void startupMods() {
    getLogger().debug("Mod startup");

    di.getInstances(AbstractMod.class).forEach(mod -> {
        try {
          mod.load();

          if (!(mod instanceof CommandMod)) {
            mods.add(mod);
          }
        } catch (Throwable t) {
          getLogger().debug("Failed to load mod {}: {}", mod.getName(), t.getMessage());
          getLogger().debug(t, t);
        }
    });
  }

  public void shutdownMods() {
    getLogger().debug("Mod shutdown");
    di.getInstances(AbstractMod.class).forEach(AbstractMod::unload);
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
