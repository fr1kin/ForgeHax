package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.OptionsSoundsScreen;
import net.minecraft.client.gui.screen.VideoSettingsScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
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
    if (MC.field_71462_r instanceof OptionsScreen
        || MC.field_71462_r instanceof VideoSettingsScreen
        || MC.field_71462_r instanceof OptionsSoundsScreen
        || MC.field_71462_r instanceof ContainerScreen
        || MC.field_71462_r instanceof IngameMenuScreen) {
      for (KeyBinding bind : keys) {
        KeyBinding.setKeyBindState(bind.getKey(), InputMappings.func_216506_a(MC.mainWindow.getHandle(), bind.getKey().getKeyCode()));
      }
    } else if (MC.field_71462_r == null) {
      for (KeyBinding bind : keys) {
        if (!InputMappings.func_216506_a(MC.mainWindow.getHandle(), bind.getKey().getKeyCode())) {
          KeyBinding.setKeyBindState(bind.getKey(), false);
        }
      }
    }
  }
}
