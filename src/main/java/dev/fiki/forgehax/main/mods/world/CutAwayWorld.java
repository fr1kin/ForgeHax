package dev.fiki.forgehax.main.mods.world;

import dev.fiki.forgehax.api.cmd.settings.FloatSetting;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.NearClippingPlaneEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "CutAwayWorld",
    description = "See through the world",
    category = Category.WORLD
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
