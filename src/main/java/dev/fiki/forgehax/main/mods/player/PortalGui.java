package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.main.util.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.types.ReflectionField;
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
  @FieldMapping(parentClass = Entity.class, value = "inPortal")
  private final ReflectionField<Boolean> Entity_inPortal;

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    Entity_inPortal.set(getLocalPlayer(), false);
  }
}
