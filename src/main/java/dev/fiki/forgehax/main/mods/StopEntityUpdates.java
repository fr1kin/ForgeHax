package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
  public void onPacketIn(PacketInboundEvent event) {
    if (event.getPacket() instanceof SEntityMetadataPacket) {
      event.setCanceled(true);
    }
  }
}
