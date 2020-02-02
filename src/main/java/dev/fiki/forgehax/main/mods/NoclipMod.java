package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class NoclipMod extends ToggleMod {
  
  public NoclipMod() {
    super(Category.PLAYER, "Noclip", false, "Enables player noclip");
  }
  
  @Override
  public void onDisabled() {
    Entity local = Common.getMountedEntityOrPlayer();
    if (local != null) {
      local.noClip = false;
    }
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    Entity local = Common.getMountedEntityOrPlayer();
    local.noClip = true;
    local.onGround = false;
    local.fallDistance = 0;
  }
}
