package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import lombok.RequiredArgsConstructor;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "FastPlace",
    description = "Place blocks faster",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class FastPlaceMod extends ToggleMod {
  private final ReflectionTools reflection;

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    reflection.Minecraft_rightClickDelayTimer.set(MC, 0);
  }
}
