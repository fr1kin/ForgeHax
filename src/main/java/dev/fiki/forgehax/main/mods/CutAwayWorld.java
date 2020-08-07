package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.asm.events.NearClippingPlaneEvent;
import dev.fiki.forgehax.main.util.cmd.settings.FloatSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "CutAwayWorld",
    description = "See through the world",
    category = Category.RENDER
)
public class CutAwayWorld extends ToggleMod {

  private final FloatSetting distance = newFloatSetting()
      .name("distance")
      .description("Near plane distance")
      .defaultTo(5.5f)
      .build();

  @SubscribeEvent
  public void nearPlaneEvent(NearClippingPlaneEvent event) {
    event.value = distance.getValue();
  }
}
