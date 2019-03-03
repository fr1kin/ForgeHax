package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Created by Babbaj on 9/5/2017. */
@RegisterMod
public class GuiMove extends ToggleMod {
  public GuiMove() {
    super(Category.MISC, "GuiMove", false, "move with a gui open");
  }

  @SubscribeEvent
  public void LocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    KeyBinding[] keys = {
      MC.gameSettings.keyBindForward,
      MC.gameSettings.keyBindBack,
      MC.gameSettings.keyBindLeft,
      MC.gameSettings.keyBindRight,
      MC.gameSettings.keyBindJump,
      MC.gameSettings.keyBindSprint
    };
    if (MC.currentScreen instanceof GuiOptions
        || MC.currentScreen instanceof GuiVideoSettings
        || MC.currentScreen instanceof GuiScreenOptionsSounds
        || MC.currentScreen instanceof GuiContainer
        || MC.currentScreen instanceof GuiIngameMenu) {
      for (KeyBinding bind : keys) {
        KeyBinding.setKeyBindState(bind.getKey(), InputMappings.isKeyDown(bind.getKey().getKeyCode()));
      }
    } else if (MC.currentScreen == null) {
      for (KeyBinding bind : keys) {
        if (!InputMappings.isKeyDown(bind.getKey().getKeyCode())) {
          KeyBinding.setKeyBindState(bind.getKey(), false);
        }
      }
    }
  }
}
