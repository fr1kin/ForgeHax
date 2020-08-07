package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.PreClientTickEvent;
import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "FullBright",
    description = "Makes everything render with maximum brightness",
    category = Category.WORLD
)
public class FullBrightMod extends ToggleMod {
  private final DoubleSetting defaultGamma = newDoubleSetting()
      .name("gamma")
      .description("default gamma to revert to")
      .defaultTo(Common.getGameSettings().gamma)
      .min(0.1D)
      .max(16D)
      .build();

  @Override
  public void onEnabled() {
    Common.getGameSettings().gamma = 16F;
  }

  @Override
  public void onDisabled() {
    Common.getGameSettings().gamma = defaultGamma.getValue();
  }

  @SubscribeEvent
  public void onClientTick(PreClientTickEvent event) {
    Common.getGameSettings().gamma = 16F;
  }
}
