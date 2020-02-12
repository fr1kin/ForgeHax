package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.NearClippingPlaneEvent;
import dev.fiki.forgehax.main.util.cmd.settings.FloatSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod
public class CutAwayWorld extends ToggleMod {

  private final FloatSetting dist = newFloatSetting()
      .name("dist")
      .description("near plane value")
      .defaultTo(1.5F)
      .build();

  public CutAwayWorld() {
    super(Category.RENDER, "CutAwayWorld", false, "CutAwayWorld");
  }


  @SubscribeEvent
  public void nearPlaneEvent(NearClippingPlaneEvent event) {
    event.value = dist.getValue();
  }
}
