package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.events.ClientTickEvent;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class FullBrightMod extends ToggleMod {
  
  public FullBrightMod() {
    super(Category.WORLD, "FullBright", false, "Makes everything render with maximum brightness");
  }
  
  private final Setting<Double> defaultGamma =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("gamma")
          .description("default gamma to revert to")
          .defaultTo(Globals.getGameSettings().gamma)
          .min(0.1D)
          .max(16D)
          .build();
  
  @Override
  public void onEnabled() {
    Globals.getGameSettings().gamma = 16F;
  }
  
  @Override
  public void onDisabled() {
    Globals.getGameSettings().gamma = defaultGamma.get();
  }
  
  @SubscribeEvent
  public void onClientTick(ClientTickEvent.Pre event) {
    Globals.getGameSettings().gamma = 16F;
  }
}
