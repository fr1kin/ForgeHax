package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.key.BindingHelper;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;

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

  @SubscribeListener
  public void onUpdate(LocalPlayerUpdateEvent event) {
    getGameSettings().keyBindForward.setPressed(true);

    if (stop_at_unloaded_chunks.getValue()) {
      if (!getWorld().isAreaLoaded(getLocalPlayer().getPosition(), 1)) {
        getGameSettings().keyBindForward.setPressed(false);
      }
    }
  }
}
