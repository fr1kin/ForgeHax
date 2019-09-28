package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.gui.ClickGui;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/**
 * Created by Babbaj on 9/5/2017.
 */
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
        || MC.currentScreen instanceof GuiIngameMenu
        || MC.currentScreen instanceof ClickGui) {
      for (KeyBinding bind : keys) {
        KeyBinding.setKeyBindState(bind.getKeyCode(), Keyboard.isKeyDown(bind.getKeyCode()));
      }
    } else if (MC.currentScreen == null) {
      for (KeyBinding bind : keys) {
        if (!Keyboard.isKeyDown(bind.getKeyCode())) {
          KeyBinding.setKeyBindState(bind.getKeyCode(), false);
        }
      }
    }
  }
}
