package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.asm.events.RestrictPlayerTablistSizeEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created by Babbaj on 9/2/2017.
 */
@RegisterMod
public class ExtraTab extends ToggleMod {

  public ExtraTab() {
    super(Category.MISC, "ExtraTab", false, "Increase max size of tab list");
  }

  @SubscribeEvent
  public void onRestrictTablistSize(RestrictPlayerTablistSizeEvent event) {
    event.setCanceled(true);
  }
}
