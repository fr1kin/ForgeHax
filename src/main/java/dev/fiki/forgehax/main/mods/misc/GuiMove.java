package dev.fiki.forgehax.main.mods.misc;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.OptionsSoundsScreen;
import net.minecraft.client.gui.screen.VideoSettingsScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "GuiMove",
    description = "move with a gui open",
    category = Category.MISC
)
public class GuiMove extends ToggleMod {
  @SubscribeListener
  public void onUpdate(LocalPlayerUpdateEvent event) {
    KeyBinding[] keys = {
        getGameSettings().keyUp,
        getGameSettings().keyDown,
        getGameSettings().keyLeft,
        getGameSettings().keyRight,
        getGameSettings().keyJump,
        getGameSettings().keySprint
    };
    if (getDisplayScreen() instanceof OptionsScreen
        || getDisplayScreen() instanceof VideoSettingsScreen
        || getDisplayScreen() instanceof OptionsSoundsScreen
        || getDisplayScreen() instanceof ContainerScreen
        || getDisplayScreen() instanceof IngameMenuScreen) {
      for (KeyBinding bind : keys) {
        KeyBinding.set(bind.getKey(),
            InputMappings.isKeyDown(getMainWindow().getWindow(), bind.getKey().getValue()));
      }
    } else if (getDisplayScreen() == null) {
      for (KeyBinding bind : keys) {
        if (InputMappings.isKeyDown(getMainWindow().getWindow(), bind.getKey().getValue())) {
          KeyBinding.set(bind.getKey(), false);
        }
      }
    }
  }
}
