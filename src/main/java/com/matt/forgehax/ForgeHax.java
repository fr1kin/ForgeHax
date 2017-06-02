package com.matt.forgehax;

import com.matt.forgehax.util.command.globals.Commands;
import com.matt.forgehax.util.container.ContainerManager;
import com.matt.forgehax.util.mod.BaseMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import static com.matt.forgehax.Wrapper.*;

@Mod(modid = ForgeHax.MODID, version = ForgeHax.VERSION, guiFactory = ForgeHax.GUI_FACTORY, clientSideOnly = true)
public class ForgeHax {
	public static final String MODID = "forgehax";
	public static final String VERSION = "1.2";
	public static final String GUI_FACTORY = "com.matt.forgehax.ForgeHaxGuiFactory";

	static {
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// NOTE: if you ever change the package name make sure this
		// is updated or mods will not load anymore
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		getModManager().addClassesInPackage("com.matt.forgehax.mods");
		getModManager().addClassesInPackage("com.matt.forgehax.mods.core");
	}

	public static String getWelcomeMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append("Running ForgeHax v");
		builder.append(VERSION);
		builder.append("\n");
		builder.append("Type .help in chat for command instructions");
		return builder.toString();
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		switch (event.getSide()) {
			case CLIENT: {
				//---- initialize configuration ----//
				// register global commands
				Commands.registerAll();

				//---- initialize mods ----//
				getModManager().loadClasses();

				//---- initialize configuration part 2 ----//
				// setup config
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
				// add shutdown hook to serialize all binds
				Runtime.getRuntime().addShutdownHook(new Thread(() -> getModManager().forEach(BaseMod::unload)));
				// registerAll mod events
				getModManager().forEach(BaseMod::startup);
				break;
			}
			default:
				break;
		}
	}
}
