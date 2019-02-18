package com.matt.forgehax.mods.services;

import static com.matt.forgehax.Helper.getModManager;
import static com.matt.forgehax.Helper.printMessageNaked;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.log.FileManager;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import joptsimple.internal.Strings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Created on 6/14/2017 by fr1kin */
@RegisterMod
public class FirstTimeRunningService extends ServiceMod {
  private static final Path STARTUP_ONCE = FileManager.getInstance().getBaseResolve("config/.once");

  private static final String getOnceFileVersion() {
    if (Files.exists(STARTUP_ONCE))
      try {
        return new String(Files.readAllBytes(STARTUP_ONCE));
      } catch (Throwable t) {
      }
    return Strings.EMPTY;
  }

  public FirstTimeRunningService() {
    super("FirstTimeRunningService");
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (!Objects.equals(ForgeHax.MOD_VERSION, getOnceFileVersion())) {
      printMessageNaked(ForgeHax.getWelcomeMessage());
      try {
        Files.write(STARTUP_ONCE, ForgeHax.MOD_VERSION.getBytes());
      } catch (IOException e) {
      }
    }
    getModManager().unload(this);
  }
}
