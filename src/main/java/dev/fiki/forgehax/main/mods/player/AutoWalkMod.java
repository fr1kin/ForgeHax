package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.key.BindingHelper;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "AutoWalk",
    description = "Automatically walks forward",
    category = Category.PLAYER
)
public class AutoWalkMod extends ToggleMod {

  public final BooleanSetting stop_at_unloaded_chunks = newBooleanSetting()
      .name("stop-at-unloaded-chunks")
      .description("Stops moving at unloaded chunks")
      .defaultTo(true)
      .build();

  @Override
  protected void onEnabled() {
    BindingHelper.disableContextHandler(getGameSettings().keyBindForward);
  }

  @Override
  public void onDisabled() {
    getGameSettings().keyBindForward.setPressed(false);
    BindingHelper.restoreContextHandler(getGameSettings().keyBindForward);
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    getGameSettings().keyBindForward.setPressed(true);

    if (stop_at_unloaded_chunks.getValue()) {
      if (!getWorld().isAreaLoaded(getLocalPlayer().getPosition(), 1)) {
        getGameSettings().keyBindForward.setPressed(false);
      }
    }
  }
}
