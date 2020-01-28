package com.matt.forgehax.mods;

import com.matt.forgehax.Globals;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

/**
 * Created by Babbaj on 9/1/2017.
 */
@RegisterMod
public class AntiHeldItemChangeMod extends ToggleMod {
  
  public AntiHeldItemChangeMod() {
    super(
        Category.PLAYER,
        "AntiHeldItemChange",
        false,
        "prevents the server from changing selected hotbar slot");
  }
  
  @SubscribeEvent
  public void onPacketReceived(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SSetSlotPacket && getLocalPlayer() != null) {
      int currentSlot = getLocalPlayer().inventory.currentItem;
      
      if (((SSetSlotPacket) event.getPacket()).getSlot() != currentSlot) {
        sendNetworkPacket(new CHeldItemChangePacket(currentSlot)); // set server's slot back to our slot

        // likely will be eating so stop right clicking
        FastReflection.Methods.KeyBinding_unPress.invoke(getGameSettings().keyBindUseItem);
        
        event.setCanceled(true);
      }
    }
  }
}
