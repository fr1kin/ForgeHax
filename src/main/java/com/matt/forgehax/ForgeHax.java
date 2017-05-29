package com.matt.forgehax;

import com.google.common.collect.Maps;
import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.mods.*;
import com.matt.forgehax.mods.core.ContainersMod;
import com.matt.forgehax.util.TickManager;
import com.matt.forgehax.util.command.events.CommandEventHandler;
import com.matt.forgehax.util.command.globals.GlobalCommands;
import com.matt.forgehax.util.container.ContainerManager;
import com.matt.forgehax.util.key.BindSerializer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Collections;
import java.util.Map;

import static com.matt.forgehax.Wrapper.*;

@Mod(modid = ForgeHax.MODID, version = ForgeHax.VERSION, guiFactory = "com.matt.forgehax.ForgeHaxGuiFactory", clientSideOnly = true)
public class ForgeHax {
	public static final String MODID = "forgehax";
	public static final String VERSION = "1.2";

	public static final String CONFIG_FILE_NAME = "settings.json";

	public static ForgeHax INSTANCE = null;

	public static ForgeHax getInstance() {
		return INSTANCE;
	}

	private File baseFolder;
	private File configFolder;
	private ForgeHaxConfig config;

	private BindSerializer bindSerializer;

	public Logger log;

	public ForgeHax() {
		INSTANCE = this;
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// NOTE: if you ever change the package name make sure this
		// is updated or mods will not load anymore
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		getModManager().addPackage("com.matt.forgehax.mods");
	}

	public Logger getLog() {
		return log;
	}

	public File getBaseDirectory() {
		return baseFolder;
	}

	public File getConfigFolder() {
		return configFolder;
	}

	public ForgeHaxConfig getConfig() {
		return config;
	}

	public void setupConfigFolder() {
		File userDir = new File(getBaseDirectory(), "users");
		userDir.mkdirs();
		configFolder = new File(userDir, "devmode");
		configFolder.mkdirs();
	}

	public void printStackTrace(Exception exception) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		getLog().error(sw);
		if (MC != null) {
			//MC.player.sendChatMessage("ERROR: " + exception.getMessage());
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		switch (event.getSide()) {
			case CLIENT: {
				//---- get log ----//
				log = event.getModLog();

				//---- initialize configuration ----//
				// create folder containing all account settings
				baseFolder = new File(event.getModConfigurationDirectory(), "forgehax");
				baseFolder.mkdirs();
				// setup folder that contains settings (supports multiple accounts)
				setupConfigFolder();
				// initialize bind serializer
				bindSerializer = new BindSerializer(getConfigFolder());
				// add shutdown hook to serialize all binds
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					if(bindSerializer != null) bindSerializer.serialize();
					getModManager().getMods().forEach(BaseMod::onUnload);
				}));
				// register global commands
				GlobalCommands.registerAll();

				//---- initialize mods ----//
				getModManager().loadPackages();

				//---- initialize configuration part 2 ----//
				// setup config
				config = new ForgeHaxConfig(new File(getConfigFolder(), CONFIG_FILE_NAME));
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
				// registerAll config
				MinecraftForge.EVENT_BUS.register(config);
				// registerAll event handler
				MinecraftForge.EVENT_BUS.register(new ForgeHaxEventHandler());
				// registerAll lag compenstator
				CommandEventHandler.register();
				TickManager.getInstance().registerEventHandler();
				// registerAll mod events
				getModManager().getMods().forEach(mod -> {
					if (mod.isEnabled()) {
						mod.onEnabled();
						mod.register();
					}
				});
				// load all previous binds
				bindSerializer.deserialize();
				break;
			}
			default:
				break;
		}
	}
}
