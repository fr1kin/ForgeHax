package com.matt.forgehax;

import com.matt.forgehax.util.event.EventBus;
import com.matt.forgehax.util.event.ForgehaxEventBus;
import com.matt.forgehax.util.mod.BaseMod;
import net.minecraftforge.fml.common.Mod;

import static com.matt.forgehax.Helper.getFileManager;
import static com.matt.forgehax.Helper.getModManager;

@Mod(modid = ForgeHax.MOD_ID, clientSideOnly = true)
public class ForgeHax {
	public static final String MOD_ID 			= "forgehax";
	public static final String MOD_VERSION 		= ForgeHaxProperties.getVersion();

	public static final EventBus EVENT_BUS = new ForgehaxEventBus();

	public static String getWelcomeMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append("Running ForgeHax v");
		builder.append(MOD_VERSION);
		builder.append("\n");
		builder.append("Type .help in chat for command instructions");
		return builder.toString();
	}

	public static void init() {
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // NOTE: if you ever change the package name make sure this
        // is updated or mods will not load anymore
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        getModManager().searchPackage("com.matt.forgehax.mods.*");
        getModManager().searchPluginDirectory(getFileManager().getBaseDirectory().toPath().resolve("plugins"));

        //---- initialize mods ----//
        getModManager().loadAll();

        // add shutdown hook to serialize all binds
        Runtime.getRuntime().addShutdownHook(new Thread(() -> getModManager().forEach(BaseMod::unload)));
        // registerAll mod events
        getModManager().forEach(BaseMod::load);
    }

}
