package dev.fiki.forgehax.main;

import com.google.common.base.Strings;
import dev.fiki.forgehax.api.FileManager;
import dev.fiki.forgehax.api.cmd.RootCommand;
import dev.fiki.forgehax.api.draw.BufferProvider;
import dev.fiki.forgehax.api.event.EventBus;
import dev.fiki.forgehax.api.log.ForgeHaxLog4J2Configuration;
import dev.fiki.forgehax.api.modloader.ModManager;
import dev.fiki.forgehax.api.modloader.di.DependencyInjector;
import dev.fiki.forgehax.api.modloader.di.providers.ReflectionProviders;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import dev.fiki.forgehax.main.ui.ConsoleInterface;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Mod("forgehax")
@Getter
@Log4j2
public class ForgeHax {
  @Getter(AccessLevel.PACKAGE)
  private static ForgeHax instance = null;

  private final Path baseDirectory;

  private ConfigProperties configProperties;

  private RootCommand rootCommand;
  private DependencyInjector dependencyInjector;
  private ModManager modManager;
  private FileManager fileManager;

  private ExecutorService asyncExecutorService;
  private ExecutorService pooledExecutorService;

  private BufferProvider bufferProvider;

  private EventBus eventBus;

  public ForgeHax() {
    instance = this;

    // directory where all config and forgehax related files should be stored
    baseDirectory = Paths.get("forgehax");

    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
  }

  public void setup(final FMLClientSetupEvent event) {
    ForgeHaxLog4J2Configuration.create();
    try {
      configProperties = new ConfigProperties();

      DependencyInjector di = (dependencyInjector = new DependencyInjector());
      di.addInstance(dependencyInjector);

      di.addInstance(this, "forgehax");
      di.addInstance(Minecraft.getInstance(), "mc");
      di.addInstance(log, "logger");

      rootCommand = new RootCommand();
      rootCommand.setConfigurationDirectory(getBaseDirectory().resolve("config"));
      di.addInstance(rootCommand);

      modManager = new ModManager(dependencyInjector);
      fileManager = new FileManager();
      di.addInstance(modManager);
      di.addInstance(fileManager);

      asyncExecutorService = Executors.newSingleThreadExecutor();
      pooledExecutorService = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
      di.addInstance(asyncExecutorService, ExecutorService.class, "async");
      di.addInstance(pooledExecutorService, ExecutorService.class, "threadpool");
      di.addInstance(Minecraft.getInstance(), Executor.class, "main");

      bufferProvider = new BufferProvider();
      di.addInstance(bufferProvider);

      eventBus = new EventBus();
      di.addInstance(eventBus);

      di.module(ConsoleInterface.class, "cli");
      di.module(ReflectionTools.class);

      ReflectionProviders.all(dependencyInjector);

      if (!modManager.searchPackage("dev.fiki.forgehax.main.commands")
          || !modManager.searchPackage("dev.fiki.forgehax.main.managers")
          || !modManager.searchPackage("dev.fiki.forgehax.main.services")
          || !modManager.searchPackage("dev.fiki.forgehax.main.mods")) {
        throw new Error("Failed to find mods. Verify the right package(s) are listed");
      }

      if (!modManager.searchPluginDirectory(getBaseDirectory().resolve("plugins"))) {
        log.info("No plugins loaded (this is fine)");
      }

      // inject all dependencies
      modManager.loadMods();

      // call AbstractMod::load
      modManager.startupMods();

      // add shutdown hook to serialize all settings
      ForgeHaxLog4J2Configuration.onLoggerShutdown(this::shutdown);
    } catch (Throwable t) {
      log.error("Fatal error loading ForgeHax!");
      log.error(t, t);

      // rethrow so forge warns the client about the errors
      throw new Error("ForgeHax failed to initialize", t);
    }
  }

  private void shutdown() {
    modManager.shutdownMods();

    shutdownExecutorService(asyncExecutorService);
    shutdownExecutorService(pooledExecutorService);
  }

  private void shutdownExecutorService(ExecutorService executorService) {
    executorService.shutdown();
    while (!executorService.isShutdown()) {
      try {
        executorService.awaitTermination(30, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        log.error(e, e);
      }
    }
  }

  public class ConfigProperties {
    private Properties properties = new Properties();

    private ConfigProperties() {
      try (InputStream is = getClass().getResourceAsStream("config.properties")) {
        Objects.requireNonNull(is, "Could not find resource config.properties");
        properties.load(is);
      } catch (NullPointerException e) {
        log.error(e.getMessage());
      } catch (IOException e) {
        log.error("Could not load config.properties");
        log.error(e, e);
      }
    }

    public String getVersion() {
      return Strings.nullToEmpty(properties.getProperty("forgehax.version"));
    }

    public String getMcVersion() {
      return Strings.nullToEmpty(properties.getProperty("forgehax.mc.version"));
    }

    public String getForgeVersion() {
      return Strings.nullToEmpty(properties.getProperty("forgehax.forge.version"));
    }

    public String getMcpVersion() {
      return Strings.nullToEmpty(properties.getProperty("forgehax.mcp.version"));
    }

    public String getMcpChannel() {
      return Strings.nullToEmpty(properties.getProperty("forgehax.mcp.channel"));
    }

    public String getMcpMapping() {
      return Strings.nullToEmpty(properties.getProperty("forgehax.mcp.mapping"));
    }

    public String getMcpMappingUrl() {
      return String.format("%s_%s_%s", getMcpVersion(), getMcpChannel(), getMcpMapping());
    }
  }
}
