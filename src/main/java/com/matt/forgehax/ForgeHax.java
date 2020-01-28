package com.matt.forgehax;

import static com.matt.forgehax.Helper.getFileManager;
import static com.matt.forgehax.Helper.getModManager;

import com.matt.forgehax.util.mod.BaseMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod("ForgeHax")
public class ForgeHax {
  public static final String MOD_VERSION = ForgeHaxProperties.getVersion();

  public ForgeHax() {
  }

  public static String getWelcomeMessage() {
    return String
        .format("Running ForgeHax v%s\n Type .help in chat for command instructions", MOD_VERSION);
  }

  public void setup(final FMLClientSetupEvent event) {
    getModManager().searchPackage("com.matt.forgehax.mods.*");
    getModManager().searchPluginDirectory(getFileManager().getBaseResolve("plugins"));

    getModManager().loadAll();

    // add shutdown hook to serialize all binds
    Runtime.getRuntime()
        .addShutdownHook(new Thread(() -> getModManager().forEach(BaseMod::unload)));

    // registerAll mod events
    getModManager().forEach(BaseMod::load);
  }
}
