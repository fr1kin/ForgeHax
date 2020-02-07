package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.*;

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
