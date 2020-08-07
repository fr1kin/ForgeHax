package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "StopEntityUpdates",
    description = "Prevent entity metadata update packets from being processed",
    category = Category.MISC
)
public class StopEntityUpdates extends ToggleMod {

  @SubscribeEvent
  public void onPacketIn(PacketInboundEvent event) {
    if (event.getPacket() instanceof SEntityMetadataPacket) {
      event.setCanceled(true);
    }
  }
}
