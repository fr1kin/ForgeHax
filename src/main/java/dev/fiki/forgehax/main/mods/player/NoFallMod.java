package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.extension.GeneralEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.minecraft.network.play.client.CPlayerPacket;

import static dev.fiki.forgehax.main.Common.getNetworkManager;

@RegisterMod(
    name = "NoFall",
    description = "Prevents fall damage from being taken",
    category = Category.PLAYER
)
@RequiredArgsConstructor
@ExtensionMethod({GeneralEx.class})
public class NoFallMod extends ToggleMod {
  @SubscribeListener
  public void onPacketSend(LocalPlayerUpdateEvent event) {
    if (event.getPlayer().fallDistance > 1) {
      getNetworkManager().dispatchNetworkPacket(new CPlayerPacket(true));
    }
  }
}
