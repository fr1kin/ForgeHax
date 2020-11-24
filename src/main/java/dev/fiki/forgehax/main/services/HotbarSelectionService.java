package dev.fiki.forgehax.main.services;

import dev.fiki.forgehax.api.events.PreClientTickEvent;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.mod.ServiceMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;
import static dev.fiki.forgehax.main.Common.isInWorld;

@RegisterMod
@ExtensionMethod({LocalPlayerEx.class})
public class HotbarSelectionService extends ServiceMod {
  @SubscribeEvent
  public void onClientTick(PreClientTickEvent event) {
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
