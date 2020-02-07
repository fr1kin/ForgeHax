package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.screen.SleepInMultiplayerScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class BedModeMod extends ToggleMod {

  public BedModeMod() {
    super(Category.PLAYER, "BedMode", false, "Sleep walking");
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    FastReflection.Fields.PlayerEntity_sleeping.set(Common.getLocalPlayer(), false);
    FastReflection.Fields.PlayerEntity_sleepTimer.set(Common.getLocalPlayer(), 0);
  }

  @SubscribeEvent
  public void onGuiUpdate(GuiOpenEvent event) {
    if (event.getGui() instanceof SleepInMultiplayerScreen) {
      event.setCanceled(true);
    }
  }
}
