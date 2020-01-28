package com.matt.forgehax.mods;

import com.matt.forgehax.Globals;
import com.matt.forgehax.events.ClientTickEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

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
          .defaultTo(getGameSettings().gamma)
          .min(0.1D)
          .max(16D)
          .build();
  
  @Override
  public void onEnabled() {
    getGameSettings().gamma = 16F;
  }
  
  @Override
  public void onDisabled() {
    getGameSettings().gamma = defaultGamma.get();
  }
  
  @SubscribeEvent
  public void onClientTick(ClientTickEvent.Pre event) {
    getGameSettings().gamma = 16F;
  }
}
