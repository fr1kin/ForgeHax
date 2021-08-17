package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;

import static dev.fiki.forgehax.main.Common.getMountedEntityOrPlayer;

@RegisterMod(
    name = "Noclip",
    description = "Enables player noclip",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class NoclipMod extends ToggleMod {
  private final ReflectionTools reflection;

  @Override
  public void onDisabled() {
    Entity local = getMountedEntityOrPlayer();
    if (local != null) {
      local.noPhysics = false;
    }
  }

  @SubscribeListener
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    Entity local = getMountedEntityOrPlayer();
    local.noPhysics = true;
    local.fallDistance = 0;
    reflection.Entity_onGround.set(local, false);
  }
}
