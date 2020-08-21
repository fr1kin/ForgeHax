package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.main.util.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.ReflectionTools;
import lombok.RequiredArgsConstructor;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.MC;

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
