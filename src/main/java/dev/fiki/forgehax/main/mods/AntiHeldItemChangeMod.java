package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.types.ReflectionMethod;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
  @MethodMapping(parentClass = KeyBinding.class, value = "unpressKey")
  private final ReflectionMethod<Void> KeyBinding_unpressKey;

  @SubscribeEvent
  public void onPacketReceived(PacketInboundEvent event) {
    if (event.getPacket() instanceof SSetSlotPacket && getLocalPlayer() != null) {
      int currentSlot = getLocalPlayer().inventory.currentItem;

      if (((SSetSlotPacket) event.getPacket()).getSlot() != currentSlot) {
        sendNetworkPacket(new CHeldItemChangePacket(currentSlot)); // set server's slot back to our slot

        // likely will be eating so stop right clicking
        KeyBinding_unpressKey.invoke(getGameSettings().keyBindUseItem);

        event.setCanceled(true);
      }
    }
  }
}
