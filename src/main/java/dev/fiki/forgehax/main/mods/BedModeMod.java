package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.types.ReflectionField;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screen.SleepInMultiplayerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "BedMode",
    description = "Sleep walking",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class BedModeMod extends ToggleMod {
  @FieldMapping(parentClass = PlayerEntity.class, value = "sleepTimer")
  public final ReflectionField<Integer> PlayerEntity_sleepTimer;

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    PlayerEntity_sleepTimer.set(Common.getLocalPlayer(), 0);
  }

  @SubscribeEvent
  public void onGuiUpdate(GuiOpenEvent event) {
    if (event.getGui() instanceof SleepInMultiplayerScreen) {
      event.setCanceled(true);
    }
  }
}
