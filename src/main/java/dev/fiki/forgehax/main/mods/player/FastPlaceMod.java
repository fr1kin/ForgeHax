package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import lombok.RequiredArgsConstructor;

@RegisterMod(
    name = "FastPlace",
    description = "Place blocks faster",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class FastPlaceMod extends ToggleMod {
  private final ReflectionTools reflection;

  @SubscribeListener
  public void onUpdate(LocalPlayerUpdateEvent event) {
    reflection.Minecraft_rightClickDelay.set(MC, 0);
  }
}
