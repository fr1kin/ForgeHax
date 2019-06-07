package com.matt.forgehax;

import static com.matt.forgehax.Helper.getFileManager;
import static com.matt.forgehax.Helper.getModManager;

import com.matt.forgehax.util.mod.BaseMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ForgeHax.MOD_ID)
public class ForgeHax {
  public static final String MOD_ID = "forgehax";
  public static final String MOD_VERSION = ForgeHaxProperties.getVersion();

  static {
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // NOTE: if you ever change the package name make sure this
    // is updated or mods will not load anymore
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    getModManager().searchPackage("com.matt.forgehax.mods.*");
    getModManager().searchPluginDirectory(getFileManager().getBaseResolve("plugins"));
  }

  public ForgeHax() {
    System.out.println("Launching ForgeHax");
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
  }

  public static String getWelcomeMessage() {
    StringBuilder builder = new StringBuilder();
    builder.append("Running ForgeHax v");
    builder.append(MOD_VERSION);
    builder.append("\n");
    builder.append("Type .help in chat for command instructions");
    return builder.toString();
  }

  public void preInit(FMLCommonSetupEvent event) {
    // ---- initialize mods ----//
    getModManager().loadAll();
  }

  public void init(FMLClientSetupEvent event) {
    // add shutdown hook to serialize all binds
    Runtime.getRuntime().addShutdownHook(new Thread(() -> getModManager().forEach(BaseMod::unload)));
    // registerAll mod events
    getModManager().forEach(BaseMod::load);
  }
}
