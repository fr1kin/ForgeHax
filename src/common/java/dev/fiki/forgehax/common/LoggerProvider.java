package dev.fiki.forgehax.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.nio.file.Paths;

@Builder
@AllArgsConstructor
public class LoggerProvider {
  private final Class<?> contextClass;
  private final String label;

  public Logger getLogger() {
    org.apache.logging.log4j.core.Logger coreLogger =
        (org.apache.logging.log4j.core.Logger) LogManager.getLogger(contextClass);

    PatternLayout layout = PatternLayout.newBuilder()
        .withPattern("%highlight{[%d{HH:mm:ss}][%level][%c{1}][%t][%C{1}::%M@%L]: %m%n}")
        .build();

    // create console appender
//    ConsoleAppender systemAppender = ConsoleAppender.newBuilder()
//        .setName("ForgeHaxConsoleAppender_" + label)
//        .setIgnoreExceptions(false)
//        .setLayout(layout)
//        .build();
//
//    systemAppender.start();
//    coreLogger.addAppender(systemAppender);

    String logs = Paths.get("forgehax").resolve("logs").toString();

    // create file appender
    RollingFileAppender fileAppender = RollingFileAppender.newBuilder()
        .withFileName(new StringBuilder(logs)
            .append('/')
            .append("latest-")
            .append(label)
            .append(".log")
            .toString())
        .withFilePattern(new StringBuilder(logs)
            .append('/')
            .append(label)
            .append("-%d{yyyy-MM-dd_HH-mm}.log.gz")
            .toString())
        .withStrategy(DefaultRolloverStrategy.newBuilder()
            .withMax("5")
            .withFileIndex("max")
            .build())
        .withPolicy(OnStartupTriggeringPolicy.createPolicy(1))
        // default max file size
        .withPolicy(SizeBasedTriggeringPolicy.createPolicy(null))
        .withPolicy(TimeBasedTriggeringPolicy.newBuilder()
            .withInterval(1)
            .withModulate(true)
            .build())
        .setName("ForgeHaxFileAppender_" + label)
        .setIgnoreExceptions(false)
        .setLayout(layout)
        .build();

    fileAppender.start();

    // add the new appenders
    coreLogger.addAppender(fileAppender);

    Level level = Level.getLevel(System.getProperty("forgehax.logging.level", "info").toUpperCase());
    coreLogger.setLevel(level);

    return coreLogger;
  }
}
