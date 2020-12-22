package dev.fiki.forgehax.api.modloader;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import dev.fiki.forgehax.api.FileHelper;
import dev.fiki.forgehax.api.classloader.AbstractClassLoader;
import dev.fiki.forgehax.api.classloader.ClassLoaderHelper;
import dev.fiki.forgehax.api.classloader.CustomClassLoaders;
import dev.fiki.forgehax.api.mod.AbstractMod;
import dev.fiki.forgehax.api.mod.CommandMod;
import dev.fiki.forgehax.api.modloader.di.DependencyInjector;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created on 5/16/2017 by fr1kin
 */
@RequiredArgsConstructor
@Log4j2
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
      log.error("Failed to search package \"{}\"", packageDir);
      log.error(e, e);
    }
    return false;
  }

  public boolean searchPlugin(Path jar, String packageDir) {
    if (!Files.exists(jar)) {
      throw new IllegalArgumentException("path must lead to an existing jar file");
    }
    try {
      log.debug("Loading plugin jar \"{}\"", jar.toAbsolutePath());
      FileSystem fs = FileHelper.newFileSystem(jar);
      ClassLoader classLoader = CustomClassLoaders.newFsClassLoader(getFMLClassLoader(), fs);

      filterClassPaths(classLoader, ClassLoaderHelper.getClassPathsInJar(jar, packageDir))
          .forEach(di::module);

      return true;
    } catch (IOException e) {
      log.error("Failed to search plugin jarfile \"{}\" -> \"{}\"",
          jar.toAbsolutePath(), packageDir);
      log.error(e, e);
      return false;
    }
  }

  public boolean searchPluginDirectory(Path directory, String packageDir) {
    if (!Files.exists(directory)) {
      log.warn("plugin directory \"{}\" does not exist!", directory.toAbsolutePath());
      return false;
    }
    if (!Files.isDirectory(directory)) {
      log.warn("\"{}\" is not a directory", directory.toAbsolutePath());
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
      log.error("Failed to search plugin directory \"{}\"", directory.toAbsolutePath());
      log.error(e, e);
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
            log.warn("Failed to load mod {}", dep.getTargetClass().getSimpleName());
            log.warn(t, t);
          }
        });
  }

  public void startupMods() {
    log.debug("Mod startup");

    di.getInstances(AbstractMod.class).forEach(mod -> {
      try {
        mod.load();

        if (!(mod instanceof CommandMod)) {
          mods.add(mod);
        }
      } catch (Throwable t) {
        log.debug("Failed to load mod {}: {}", mod.getName(), t.getMessage());
        log.debug(t, t);
      }
    });
  }

  public void shutdownMods() {
    log.debug("Mod shutdown");
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
