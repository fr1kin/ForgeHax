package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.util.reflection.FastReflection;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class FastBreak extends ToggleMod {

  public FastBreak() {
    super(Category.PLAYER, "FastBreak", false, "Fast break retard");
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (Common.getPlayerController() != null) {
      FastReflection.Fields.PlayerController_blockHitDelay.set(Common.getPlayerController(), 0);
    }
  }
}
