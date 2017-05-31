package com.matt.forgehax;

import com.matt.forgehax.util.TickManager;
import com.matt.forgehax.util.command.events.CommandEventHandler;
import com.matt.forgehax.util.command.globals.GlobalCommands;
import com.matt.forgehax.util.container.ContainerManager;
import com.matt.forgehax.util.key.BindSerializer;
import com.matt.forgehax.util.mod.BaseMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.*;

import static com.matt.forgehax.Wrapper.*;

@Mod(modid = ForgeHax.MODID, version = ForgeHax.VERSION, guiFactory = ForgeHax.GUI_FACTORY, clientSideOnly = true)
public class ForgeHax {
	public static final String MODID = "forgehax";
	public static final String VERSION = "1.2";
	public static final String GUI_FACTORY = "com.matt.forgehax.ForgeHaxGuiFactory";

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		switch (event.getSide()) {
			case CLIENT: {
				//---- initialize configuration ----//
				// create folder containing all account settings
				Wrapper.getFileManager().setBaseDirectory(new File(event.getModConfigurationDirectory(), "forgehax"));

				// setup folder that contains settings
				getFileManager().setConfigDirectory(getFileManager().getFileInBaseDirectory("users", "devmode"));
				// initialize bind serializer
				BindSerializer.getInstance().initialize();
				// add shutdown hook to serialize all binds
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					BindSerializer.getInstance().serialize();
					getModManager().getMods().forEach(BaseMod::onUnload);
				}));
				// register global commands
				GlobalCommands.registerAll();

				//---- initialize mods ----//
				// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				// NOTE: if you ever change the package name make sure this
				// is updated or mods will not load anymore
				// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				getModManager().addPackage("com.matt.forgehax.mods");
				getModManager().loadPackages();

				//---- initialize configuration part 2 ----//
				// setup config
				getFileManager().setForgeConfiguration(new Configuration(getFileManager().getFileInConfigDirectory("settings.json")));
				getConfigurationHandler().initialize();
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
				MinecraftForge.EVENT_BUS.register(getConfigurationHandler());
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
				BindSerializer.getInstance().deserialize();
				break;
			}
			default:
				break;
		}
	}
}
