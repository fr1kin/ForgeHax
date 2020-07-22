package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.entity.Entity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.getMountedEntityOrPlayer;

@RegisterMod
public class NoclipMod extends ToggleMod {

  public NoclipMod() {
    super(Category.PLAYER, "Noclip", false, "Enables player noclip");
  }

  @Override
  public void onDisabled() {
    Entity local = getMountedEntityOrPlayer();
    if (local != null) {
      local.noClip = false;
    }
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    Entity local = getMountedEntityOrPlayer();
    local.noClip = true;
    local.fallDistance = 0;
    FastReflection.Fields.Entity_onGround.set(local, false);
  }
}
