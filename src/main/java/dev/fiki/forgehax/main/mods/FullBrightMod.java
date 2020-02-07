package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.ClientTickEvent;
import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class FullBrightMod extends ToggleMod {

  public FullBrightMod() {
    super(Category.WORLD, "FullBright", false, "Makes everything render with maximum brightness");
  }

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
  public void onClientTick(ClientTickEvent.Pre event) {
    Common.getGameSettings().gamma = 16F;
  }
}
