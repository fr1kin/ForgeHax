package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Babbaj on 9/1/2017.
 */
@RegisterMod
public class AntiHeldItemChangeMod extends ToggleMod {
  
  public AntiHeldItemChangeMod() {
    super(
        Category.PLAYER,
        "AntiHeldChange",
        false,
        "prevents the server from changing selected hotbar slot");
  }
  
  @SubscribeEvent
  public void onPacketReceived(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPacketSetSlot && getLocalPlayer() != null) {
      int currentSlot = getLocalPlayer().inventory.currentItem;
      
      if (((SPacketSetSlot) event.getPacket()).getSlot() != currentSlot) {
        getNetworkManager()
            .sendPacket(
                new CPacketHeldItemChange(currentSlot)); // set server's slot back to our slot
        FastReflection.Methods.KeyBinding_unPress.invoke(
            MC.gameSettings.keyBindUseItem); // likely will eating so stop right clicking
        
        event.setCanceled(true);
      }
    }
  }
}
