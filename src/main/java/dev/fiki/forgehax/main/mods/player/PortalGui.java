package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "PortalGui",
    description = "Guis work while in portals",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class PortalGui extends ToggleMod {
  @MapField(parentClass = Entity.class, value = "inPortal")
  private final ReflectionField<Boolean> Entity_inPortal;

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    Entity_inPortal.set(getLocalPlayer(), false);
  }
}
