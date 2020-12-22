package dev.fiki.forgehax.api.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.*;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.spi.LoggerContextFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ForgeHaxLog4J2Configuration {
  private static final AtomicBoolean activated = new AtomicBoolean(false);

  public static Path getLogDirectory() {
    return Paths.get("forgehax").resolve("logs");
  }

  public static Level getLogLevel() {
    String prop = System.getProperty("forgehax.logging.level");
    if (prop != null && !prop.isEmpty()) {
      return Level.getLevel(prop.toUpperCase());
    } else {
      // use the default log level
      return LogManager.getRootLogger().getLevel();
    }
  }

  public static String getLoggerName() {
    return "dev.fiki.forgehax";
  }

  public static void create() {
    if (activated.compareAndSet(false, true)) {
      update();
    }
  }

  public static Logger createAndGet(Supplier<Logger> supplier) {
    create();
    return supplier.get();
  }

  public static void onLoggerShutdown(Runnable runnable) {
    final LoggerContextFactory contextFactory = LogManager.getFactory();

    if (contextFactory instanceof Log4jContextFactory) {
      Log4jContextFactory ctxFactory = (Log4jContextFactory) contextFactory;
      ctxFactory.addShutdownCallback(runnable);
    }
  }

  private static void update() {
    final String logName = getLoggerName();
    final Path logDir = getLogDirectory();
    final Level logLevel = getLogLevel();

    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();

    Appender[] appenders = Stream.of(newSystemAppender(), newFileAppender(logDir))
        .peek(Appender::start)
        .peek(config::addAppender)
        .toArray(Appender[]::new);

    AppenderRef[] appenderRefs = Arrays.stream(appenders)
        .map(app -> AppenderRef.createAppenderRef(app.getName(), null, null))
        .toArray(AppenderRef[]::new);

    LoggerConfig loggerConfig = LoggerConfig.createLogger(false, logLevel, logName, "true",
        appenderRefs, new Property[0], config, null);

    for (Appender app : appenders) {
      loggerConfig.addAppender(app, null, null);
    }

    config.addLogger(logName, loggerConfig);
    ctx.updateLoggers();
  }

  private static Appender newFileAppender(Path logOutputDir) {
    return RollingFileAppender.newBuilder()
        .withFileName(logOutputDir.resolve("latest.log").toString())
        .withFilePattern(logOutputDir.resolve("%d{yyyy-MM-dd}-%i.log.gz").toString())
        .withStrategy(DefaultRolloverStrategy.newBuilder()
            .withMax("99")
            .withFileIndex("min")
            .build())
        .withPolicy(CompositeTriggeringPolicy.createPolicy(
            OnStartupTriggeringPolicy.createPolicy(1),
            TimeBasedTriggeringPolicy.newBuilder()
                .withInterval(1)
                .withModulate(true)
                .build(),
            SizeBasedTriggeringPolicy.createPolicy(null)
        ))
        .setName("ForgeHax_File")
        .setIgnoreExceptions(false)
        .setLayout(createFileLayout())
        .build();
  }

  private static Appender newSystemAppender() {
    return ConsoleAppender.newBuilder()
        .setName("ForgeHax_Sys")
        .setIgnoreExceptions(false)
        .setLayout(createConsoleLayout())
        .build();
  }

  private static PatternLayout createConsoleLayout() {
    return PatternLayout.newBuilder()
        .withPattern("%highlight{[%d{HH:mm:ss} %level %c{1}::%M@%L]: %m%n}")
        .build();
  }

  private static PatternLayout createFileLayout() {
    return PatternLayout.newBuilder()
        .withPattern("[%d{HH:mm:ss} %level %c{1}::%M@%L]: %m%n")
        .withCharset(StandardCharsets.UTF_8)
        .build();
  }
}
