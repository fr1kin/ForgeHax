package dev.fiki.forgehax.main.mods.misc;

import dev.fiki.forgehax.asm.events.RestrictPlayerTablistSizeEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "ExtraTab",
    description = "Increase max size of tab list",
    category = Category.MISC
)
public class ExtraTab extends ToggleMod {
  @SubscribeEvent
  public void onRestrictTablistSize(RestrictPlayerTablistSizeEvent event) {
    event.setCanceled(true);
  }
}
