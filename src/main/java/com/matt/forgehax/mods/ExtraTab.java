package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

/**
 * Created by Babbaj on 9/2/2017.
 * tonio made it longer!
 */
@RegisterMod
public class ExtraTab extends ToggleMod {

  private final Setting<Boolean> smart =
  getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("auto")
      .description("Don't increase tablist when at 60 players or less")
      .defaultTo(true)
      .build();
      
  
  public ExtraTab() {
    super(Category.MISC, "ExtraTab", false, "Increase max size of tab list");
  }
  
  @Override
  public void onEnabled() {
    ForgeHaxHooks.doIncreaseTabListSize = true;
  }
  
  @Override
  public void onDisabled() {
    ForgeHaxHooks.doIncreaseTabListSize = false;
  }

  @SubscribeEvent
  public void onTick(PlayerTickEvent event) {
    if (smart.get() && MC.getConnection() != null && MC.getConnection().getPlayerInfoMap() != null) {
      ForgeHaxHooks.doIncreaseTabListSize =
        (MC.getConnection().getPlayerInfoMap().size() > 60);
    }
  }
}
