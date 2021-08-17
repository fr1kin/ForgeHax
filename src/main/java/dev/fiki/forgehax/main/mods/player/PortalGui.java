package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "PortalGui",
    description = "Guis work while in portals",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class PortalGui extends ToggleMod {
  @MapField(parentClass = Entity.class, value = "isInsidePortal")
  private final ReflectionField<Boolean> Entity_isInsidePortal;

  @SubscribeListener
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    Entity_isInsidePortal.set(getLocalPlayer(), false);
  }
}
