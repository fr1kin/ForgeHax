package dev.fiki.forgehax.main.services;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.game.PreGameTickEvent;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.mod.ServiceMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.experimental.ExtensionMethod;
import lombok.val;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;
import static dev.fiki.forgehax.main.Common.isInWorld;

@RegisterMod
@ExtensionMethod({LocalPlayerEx.class})
public class HotbarSelectionService extends ServiceMod {
  @SubscribeListener
  public void onClientTick(PreGameTickEvent event) {
    val data = LocalPlayerEx.getSelectedItemData();

    if (isInWorld()) {
      if (data.getOriginalIndex() != -1 && data.testReset()) {
        data.resetSelected(getLocalPlayer().getInventory());
      }
      data.tick();
    } else {
      data.reset();
    }
  }
}
