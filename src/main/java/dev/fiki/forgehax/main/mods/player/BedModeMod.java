package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import dev.fiki.forgehax.main.Common;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screen.SleepInMultiplayerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.GuiOpenEvent;

@RegisterMod(
    name = "BedMode",
    description = "Sleep walking",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class BedModeMod extends ToggleMod {
  @MapField(parentClass = PlayerEntity.class, value = "sleepCounter")
  public final ReflectionField<Integer> PlayerEntity_sleepCounter;

  @SubscribeListener
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    PlayerEntity_sleepCounter.set(Common.getLocalPlayer(), 0);
  }

  @SubscribeListener
  public void onGuiUpdate(GuiOpenEvent event) {
    if (event.getGui() instanceof SleepInMultiplayerScreen) {
      event.setCanceled(true);
    }
  }
}
