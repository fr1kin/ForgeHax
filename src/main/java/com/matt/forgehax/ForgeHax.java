package com.matt.forgehax;

import com.matt.forgehax.util.mod.BaseMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import static com.matt.forgehax.Helper.getModManager;

@Mod(modid = ForgeHax.MOD_ID, clientSideOnly = true)
public class ForgeHax {
	public static final String MOD_ID 			= "forgehax";
	public static final String MOD_VERSION 		= ForgeHaxProperties.getVersion();

	static {
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// NOTE: if you ever change the package name make sure this
		// is updated or mods will not load anymore
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		getModManager().addClassesInPackage("com.matt.forgehax.mods");
		getModManager().addClassesInPackage("com.matt.forgehax.mods.commands");
		getModManager().addClassesInPackage("com.matt.forgehax.mods.services");
		getModManager().addClassesInPackage("com.matt.forgehax.mods.services.tasks");
		getModManager().getPluginClasses();
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
}
