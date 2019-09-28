package com.matt.forgehax;

import static com.matt.forgehax.Helper.getFileManager;
import static com.matt.forgehax.Helper.getModManager;

import com.matt.forgehax.util.mod.BaseMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = ForgeHax.MOD_ID, clientSideOnly = true)
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
  
  public static String getWelcomeMessage() {
    return String
        .format("Running ForgeHax v%s\n Type .help in chat for command instructions", MOD_VERSION);
  }
  
  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    if (event.getSide() == Side.CLIENT) {
      // ---- initialize mods ----//
      getModManager().loadAll();
    }
  }
  
  @Mod.EventHandler
  public void init(FMLInitializationEvent event) {
    if (event.getSide() == Side.CLIENT) {
      // add shutdown hook to serialize all binds
      Runtime.getRuntime()
          .addShutdownHook(new Thread(() -> getModManager().forEach(BaseMod::unload)));
      
      // registerAll mod events
      getModManager().forEach(BaseMod::load);
    }
  }
}
