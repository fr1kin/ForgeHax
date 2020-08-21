package dev.fiki.forgehax.main.services;

import dev.fiki.forgehax.main.util.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import joptsimple.internal.Strings;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod
public class FirstTimeRunningService extends ServiceMod {
  
  private static final Path STARTUP_ONCE = getFileManager().getBaseResolve("config/.once");
  
  private static final String getOnceFileVersion() {
    if (Files.exists(STARTUP_ONCE)) {
      try {
        return new String(Files.readAllBytes(STARTUP_ONCE));
      } catch (Throwable t) {
      }
    }
    return Strings.EMPTY;
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (!Objects.equals(getConfigProperties().getVersion(), getOnceFileVersion())) {
      printInform(String.format("Running ForgeHax v%s\n Type .help in chat for command instructions",
          getConfigProperties().getVersion()));
      try {
        Files.write(STARTUP_ONCE, getConfigProperties().getVersion().getBytes());
      } catch (IOException e) {}
    }
    unload();
  }
}
