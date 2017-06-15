package com.matt.forgehax;

import com.matt.forgehax.util.container.ContainerManager;
import com.matt.forgehax.util.mod.BaseMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.InputStream;
import java.util.Properties;

import static com.matt.forgehax.Helper.getModManager;

@Mod(modid = ForgeHax.MOD_ID, name = ForgeHax.MOD_NAME, clientSideOnly = true)
public class ForgeHax {
	public static final String MOD_ID 			= "forgehax";
	public static final String MOD_NAME 		= "ForgeHax";
	public static final String MOD_VERSION 		= ConfigProperties.getConfigProperties().getProperty("forgehax.version");

	static {
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// NOTE: if you ever change the package name make sure this
		// is updated or mods will not load anymore
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		getModManager().addClassesInPackage("com.matt.forgehax.mods");
		getModManager().addClassesInPackage("com.matt.forgehax.mods.services");
		getModManager().addClassesInPackage("com.matt.forgehax.mods.commands");
	}

	public static String getWelcomeMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append("Running ForgeHax v");
		builder.append(MOD_VERSION);
		builder.append("\n");
		builder.append("Type .help in chat for command instructions");
		return builder.toString();
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		switch (event.getSide()) {
			case CLIENT: {
				//---- initialize mods ----//
				getModManager().loadClasses();

				//---- initialize configuration ----//
				// init containers
				ContainerManager.initialize();
			}
			default:
				break;
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		switch (event.getSide()) {
			case CLIENT: {
				// add shutdown hook to serialize all binds
				Runtime.getRuntime().addShutdownHook(new Thread(() -> getModManager().forEach(BaseMod::unload)));
				// registerAll mod events
				getModManager().forEach(BaseMod::load);
				break;
			}
			default:
				break;
		}
	}

	private static class ConfigProperties {
		private static final Properties CONFIG_PROPERTIES = new Properties();

		static {
			InputStream input = null;
			try {
				input = ConfigProperties.class.getResourceAsStream("config.properties");
				CONFIG_PROPERTIES.load(input);
			} catch (Throwable t) {
				Helper.getLog().error("Failed to load resource config.properties");
			} finally {
				if(input != null) try {
					input.close();
				} catch (Throwable t) {
					;
				}
			}
		}

		public static Properties getConfigProperties() {
			return CONFIG_PROPERTIES;
		}
	}
}
