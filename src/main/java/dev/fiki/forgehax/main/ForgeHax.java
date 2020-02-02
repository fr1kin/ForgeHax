package dev.fiki.forgehax.main;

import com.google.common.base.Strings;
import dev.fiki.forgehax.common.LoggerProvider;
import dev.fiki.forgehax.main.util.FileManager;
import dev.fiki.forgehax.main.util.mod.BaseMod;
import dev.fiki.forgehax.main.util.mod.loader.ModManager;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

@Mod("forgehax")
@Getter
public class ForgeHax {
  @Getter(AccessLevel.PACKAGE)
  private static ForgeHax instance = null;

  private final Path baseDirectory;

  private Logger logger;

  private ConfigProperties configProperties;

  private ModManager modManager;
  private FileManager fileManager;

  public ForgeHax() {
    instance = this;

    // directory where all config and forgehax related files should be stored
    baseDirectory = Paths.get("forgehax");

    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
  }

  public void setup(final FMLClientSetupEvent event) {
    logger = LoggerProvider.builder()
        .contextClass(ForgeHax.class)
        .label("main")
        .build()
        .getLogger();

    try {
      configProperties = new ConfigProperties();

      modManager = new ModManager();
      fileManager = new FileManager();

      if(!getModManager().searchPackage("dev.fiki.forgehax.main.mods")) {
        logger.error("Could not find any mods to load. Verify the right package is listed");

        // ForgeHax won't do anything without mods so stop loading here
        return;
      }

      if(!getModManager().searchPluginDirectory(getBaseDirectory().resolve("plugins"))) {
        logger.info("No plugins loaded");
      }

      getModManager().loadAll();

      // add shutdown hook to serialize all binds
      Runtime.getRuntime().addShutdownHook(new Thread(() -> getModManager().forEach(BaseMod::unload)));

      // registerAll mod events
      getModManager().forEach(BaseMod::load);
    } catch (Throwable t) {
      getLogger().error("Fatal error loading ForgeHax!");
      getLogger().error(t, t);

      // rethrow so forge warns the client about the errors
      throw t;
    }
  }

  public class ConfigProperties {
    private Properties properties = new Properties();

    private ConfigProperties() {
      try(InputStream is = getClass().getResourceAsStream("config.properties")) {
        Objects.requireNonNull(is, "Could not find resource config.properties");
        properties.load(is);
      } catch (NullPointerException e) {
        logger.error(e.getMessage());
      } catch (IOException e) {
        logger.error("Could not load config.properties");
        logger.error(e, e);
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
