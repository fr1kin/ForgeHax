package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class AntiFogMod extends ToggleMod {
  
  public AntiFogMod() {
    super(Category.WORLD, "AntiFog", false, "Removes fog");
  }
  
  @SubscribeEvent
  public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
    event.setDensity(0);
    event.setCanceled(true);
  }
  
  @SubscribeEvent
  public void onFogColor(EntityViewRenderEvent.FogColors event) {
    event.setRed(55);
    event.setGreen(55);
    event.setBlue(55);
  }
}
