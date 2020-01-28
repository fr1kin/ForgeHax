package com.matt.forgehax.mods;

import com.matt.forgehax.Globals;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

/**
 * Created by Babbaj on 8/28/2017.
 */
@RegisterMod
public class PortalGui extends ToggleMod {
  
  public PortalGui() {
    super(Category.PLAYER, "PortalGui", false, "Guis work while in portals");
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    FastReflection.Fields.Entity_inPortal.set(getLocalPlayer(), false);
  }
}
