package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.asm.MapMethod;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionMethod;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SSetSlotPacket;

import static dev.fiki.forgehax.main.Common.*;

/**
 * Created by Babbaj on 9/1/2017.
 */
@RegisterMod(
    name = "AntiHeldItemChange",
    description = "Prevents the server from changing selected hotbar slot",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class AntiHeldItemChangeMod extends ToggleMod {
  @MapMethod(parentClass = KeyBinding.class, value = "release")
  private final ReflectionMethod<Void> KeyBinding_release;

  @SubscribeListener
  public void onPacketReceived(PacketInboundEvent event) {
    if (event.getPacket() instanceof SSetSlotPacket && getLocalPlayer() != null) {
      int currentSlot = getLocalPlayer().inventory.selected;

      if (((SSetSlotPacket) event.getPacket()).getSlot() != currentSlot) {
        sendNetworkPacket(new CHeldItemChangePacket(currentSlot)); // set server's slot back to our slot

        // likely will be eating so stop right clicking
        KeyBinding_release.invoke(getGameSettings().keyUse);

        event.setCanceled(true);
      }
    }
  }
}
