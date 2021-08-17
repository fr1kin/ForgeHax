package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.multiplayer.PlayerController;

import static dev.fiki.forgehax.main.Common.getPlayerController;

@RegisterMod(
    name = "FastBreak",
    description = "Break blocks faster",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class FastBreak extends ToggleMod {
  @MapField(parentClass = PlayerController.class, value = "destroyDelay")
  private final ReflectionField<Integer> PlayerController_destroyDelay;

  @SubscribeListener
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (getPlayerController() != null) {
      PlayerController_destroyDelay.set(getPlayerController(), 0);
    }
  }
}
