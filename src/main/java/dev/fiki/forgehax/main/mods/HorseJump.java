package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class HorseJump extends ToggleMod {
  
  public HorseJump() {
    super(Category.PLAYER, "HorseJump", false, "always max horse jump");
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    FastReflection.Fields.ClientPlayerEntity_horseJumpPower.set(Common.getLocalPlayer(), 1.F);
  }
}
