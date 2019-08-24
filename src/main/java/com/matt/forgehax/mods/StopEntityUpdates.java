package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class StopEntityUpdates extends ToggleMod {
  
  public StopEntityUpdates() {
    super(
      Category.MISC,
      "StopEntityUpdates",
      false,
      "Prevent entity metadata update packets from being processed");
  }
  
  @SubscribeEvent
  public void onPacketIn(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPacketEntityMetadata) {
      event.setCanceled(true);
    }
  }
}
