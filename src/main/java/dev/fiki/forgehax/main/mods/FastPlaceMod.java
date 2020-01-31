package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.util.reflection.FastReflection;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created on 9/4/2016 by fr1kin
 */
@RegisterMod
public class FastPlaceMod extends ToggleMod {
  
  public FastPlaceMod() {
    super(Category.PLAYER, "FastPlace", false, "Fast place");
  }
  
  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    FastReflection.Fields.Minecraft_rightClickDelayTimer.set(Globals.MC, 0);
  }
}
