package dev.fiki.forgehax.asm.utils;

import cpw.mods.modlauncher.ClassTransformer;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.fml.loading.ModDirTransformerDiscoverer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.apache.logging.log4j.core.filter.MarkerFilter;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

@Log4j2
public class EZ {
  public static void inject() {
    getJarPath().ifPresent(ModDirTransformerDiscoverer.getExtraLocators()::add);
  }

  public static Optional<URL> getOurJar() {
    final URL url = EZ.class.getProtectionDomain().getCodeSource().getLocation();
    final Path p;
    try {
      p = Paths.get(url.toURI());
    } catch (URISyntaxException ex) {
      throw new RuntimeException(ex);
    }

    if (p.getFileName().toString().toLowerCase().endsWith(".jar")) {
      return Optional.of(url);
    } else {
      return Optional.empty();
    }
  }

  public static Optional<Path> getJarPath() {
    return getOurJar().map(url -> {
      try {
        return Paths.get(url.toURI());
      } catch (URISyntaxException ex) {
        throw new RuntimeException(ex);
      }
    });
  }

  public static void enableClassDumping() {
    log.info("Enabling class dumping!");

    MarkerManager.getMarker("CLASSDUMP");

    try {
      Field loggerField = ClassTransformer.class.getDeclaredField("LOGGER");
      loggerField.setAccessible(true);

      Logger logger = (Logger) loggerField.get(null);
      enableDumpingProperties(logger);

      logger.getContext().addPropertyChangeListener(event -> {
        log.warn("CLASSDUMP properties changed! Attempting to revert");
        enableDumpingProperties(logger);
      });
    } catch (Throwable e) {
      log.error("Failed to activate class dumping: {}", e.getMessage());
      log.error(e, e);
    }
  }

  private static void enableDumpingProperties(Logger logger) {
    logger.setLevel(Level.TRACE);

    CompositeFilter compositeFilter = (CompositeFilter) logger.getContext().getConfiguration().getFilter();
    Objects.requireNonNull(compositeFilter, "compositeFilter");

    Filter[] filters = compositeFilter.getFiltersArray();
    Objects.requireNonNull(filters, "filters");

    for (int i = 0; i < filters.length; i++) {
      Filter filter = filters[i];
      if (filter instanceof MarkerFilter && "CLASSDUMP".equals(filter.toString())) {
        filters[i] = MarkerFilter.createFilter("CLASSDUMP", Filter.Result.ACCEPT, Filter.Result.NEUTRAL);
        return;
      }
    }

    throw new Error("Failed to find CLASSDUMP filter in CompositeFilter");
  }
}
