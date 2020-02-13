package dev.fiki.forgehax.asm.utils;

import lombok.SneakyThrows;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.DefaultErrorHandler;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Optional;

import static dev.fiki.forgehax.asm.ASMCommon.getLogger;

public class EZ {
  Logger fmlLogger;
  LAppender appender;

  @SneakyThrows
  public EZ() {
    Class<?> fmlServiceProviderClass = Class.forName("net.minecraftforge.fml.loading.FMLServiceProvider");
    Field loggerField = fmlServiceProviderClass.getDeclaredField("LOGGER");
    loggerField.setAccessible(true);

    fmlLogger = (Logger) loggerField.get(null);
    fmlLogger.addAppender(appender = new LAppender());
  }

  public static Optional<URL> getUrl() {
    final String thisPath = EZ.class.getName().replace('.', '/') + ".class";
    final URL url = EZ.class.getClassLoader().getResource(thisPath);

    try {
      URLConnection connection = url.openConnection();
      if (connection instanceof JarURLConnection) {
        return Optional.of(((JarURLConnection) connection).getJarFileURL());
      }
    } catch (IOException ex) {
      getLogger().error(ex, ex);
    }

    return Optional.empty();
  }

  @SneakyThrows
  public void disableBlocker(URL url) {
    Class<?> transformerDiscoverer = Class.forName("net.minecraftforge.fml.loading.ModDirTransformerDiscoverer");

    Field transformersField = transformerDiscoverer.getDeclaredField("transformers");
    transformersField.setAccessible(true);

    transformersField.set(null, Collections.emptyList());
  }

  class LAppender implements Appender {
    final ErrorHandler defaultHandler = new DefaultErrorHandler(this);

    @Override
    public void append(LogEvent event) {
      if (event.getMessage() != null
          && event.getMessage().getFormat() != null
          && event.getMessage().getFormat().contains("Initiating mod scan")) {
        EZ.getUrl().ifPresent(EZ.this::disableBlocker);
        fmlLogger.removeAppender(appender);
      }
    }

    @Override
    public String getName() {
      return "ForgeHaxListener";
    }

    @Override
    public Layout<? extends Serializable> getLayout() {
      return PatternLayout.createDefaultLayout();
    }

    @Override
    public boolean ignoreExceptions() {
      return false;
    }

    @Override
    public ErrorHandler getHandler() {
      return defaultHandler;
    }

    @Override
    public void setHandler(ErrorHandler handler) {
    }

    @Override
    public State getState() {
      return State.INITIALIZED;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isStarted() {
      return true;
    }

    @Override
    public boolean isStopped() {
      return false;
    }
  }
}
