package com.matt.forgehax.mods;

import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@RegisterMod
public class FullBrightMod extends ToggleMod {
  public FullBrightMod() {
    super(Category.WORLD, "FullBright", false, "Makes everything render with maximum brightness");
  }

  @Override
  public void onEnabled() {
    MC.gameSettings.gammaSetting = 16F;
  }

  @Override
  public void onDisabled() {
    MC.gameSettings.gammaSetting = 1F;
  }

  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent event) {
    MC.gameSettings.gammaSetting = 16F;
  }
}
