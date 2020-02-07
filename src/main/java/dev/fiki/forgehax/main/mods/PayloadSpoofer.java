package dev.fiki.forgehax.main.mods;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.common.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;

import java.util.Scanner;
import java.util.Set;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created on 6/2/2017 by fr1kin
 */
@RegisterMod
public class PayloadSpoofer extends ToggleMod {

  private static final Set<String> IGNORE_LIST = Sets.newHashSet();

  static {
    IGNORE_LIST.add("WDL|INIT");
    IGNORE_LIST.add("WDL|CONTROL");
    IGNORE_LIST.add("WDL|REQUEST");
  }

  public PayloadSpoofer() {
    super(Category.MISC, "PayloadSpoofer", false, "Will cancel packets sent by some mods");
  }

  private boolean isBlockedPacket(String channel, PacketBuffer buffer) {
    if (IGNORE_LIST.contains(channel)) {
      return true;
    } else if ("REGISTER" .equals(channel)) {
      Scanner scanner = new Scanner(new String(buffer.array()));
      scanner.useDelimiter("\\u0000");
      if (scanner.hasNext()) {
        String next = scanner.next();
        return !Strings.isNullOrEmpty(next) && IGNORE_LIST.contains(next);
      }
    }
    return false;
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onIncomingPacket(PacketInboundEvent event) {
    if (event.getPacket() instanceof SCustomPayloadPlayPacket) {
      String channel = ((SCustomPayloadPlayPacket) event.getPacket()).getChannelName().toString();
      PacketBuffer packetBuffer = ((SCustomPayloadPlayPacket) event.getPacket()).getBufferData();
      if (isBlockedPacket(channel, packetBuffer)) {
        event.setCanceled(true);
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onOutgoingPacket(PacketOutboundEvent event) {
    if (event.getPacket() instanceof CCustomPayloadPacket) {
      String channel = ((CCustomPayloadPacket) event.getPacket()).getName().toString();
      PacketBuffer packetBuffer = ((CCustomPayloadPacket) event.getPacket()).getInternalData();
      if (isBlockedPacket(channel, packetBuffer)) {
        event.setCanceled(true);
      }
    }
  }
}
