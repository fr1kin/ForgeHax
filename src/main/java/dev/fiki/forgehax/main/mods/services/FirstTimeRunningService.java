package dev.fiki.forgehax.main.mods.services;

import dev.fiki.forgehax.main.ForgeHax;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.FileManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import joptsimple.internal.Strings;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created on 6/14/2017 by fr1kin
 */
@RegisterMod
public class FirstTimeRunningService extends ServiceMod {
  
  private static final Path STARTUP_ONCE = FileManager.getInstance().getBaseResolve("config/.once");
  
  private static final String getOnceFileVersion() {
    if (Files.exists(STARTUP_ONCE)) {
      try {
        return new String(Files.readAllBytes(STARTUP_ONCE));
      } catch (Throwable t) {
      }
    }
    return Strings.EMPTY;
  }
  
  public FirstTimeRunningService() {
    super("FirstTimeRunningService");
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (!Objects.equals(ForgeHax.MOD_VERSION, getOnceFileVersion())) {
      Globals.printInform(ForgeHax.getWelcomeMessage());
      try {
        Files.write(STARTUP_ONCE, ForgeHax.MOD_VERSION.getBytes());
      } catch (IOException e) {}
    }
    Globals.getModManager().unload(this);
  }
}
