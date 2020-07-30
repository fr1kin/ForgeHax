package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created by Babbaj on 9/1/2017.
 */
@RegisterMod
public class AntiHeldItemChangeMod extends ToggleMod {

  public AntiHeldItemChangeMod() {
    super(Category.PLAYER,
        "AntiHeldItemChange",
        false,
        "prevents the server from changing selected hotbar slot");
  }

  @SubscribeEvent
  public void onPacketReceived(PacketInboundEvent event) {
    if (event.getPacket() instanceof SSetSlotPacket && Common.getLocalPlayer() != null) {
      int currentSlot = Common.getLocalPlayer().inventory.currentItem;

      if (((SSetSlotPacket) event.getPacket()).getSlot() != currentSlot) {
        Common.sendNetworkPacket(new CHeldItemChangePacket(currentSlot)); // set server's slot back to our slot

        // likely will be eating so stop right clicking
        FastReflection.Methods.KeyBinding_unPress.invoke(Common.getGameSettings().keyBindUseItem);

        event.setCanceled(true);
      }
    }
  }
}
