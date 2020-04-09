package dev.fiki.forgehax.asm.utils;

import net.minecraftforge.fml.loading.ModDirTransformerDiscoverer;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


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

}
