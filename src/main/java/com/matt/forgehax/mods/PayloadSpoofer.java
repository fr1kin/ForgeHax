package com.matt.forgehax.mods;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Scanner;
import java.util.Set;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Created on 6/2/2017 by fr1kin */
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
    } else if ("REGISTER".equals(channel)) {
      Scanner scanner = new Scanner(new String(buffer.array()));
      scanner.useDelimiter("\\u0000");
      if (scanner.hasNext()) {
        String next = scanner.next();
        if (!Strings.isNullOrEmpty(next) && IGNORE_LIST.contains(next)) return true;
      }
    }
    return false;
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onIncomingPacket(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPacketCustomPayload) {
      String channel = ((SPacketCustomPayload) event.getPacket()).getChannelName();
      PacketBuffer packetBuffer = ((SPacketCustomPayload) event.getPacket()).getBufferData();
      if (isBlockedPacket(channel, packetBuffer)) event.setCanceled(true);
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onOutgoingPacket(PacketEvent.Outgoing.Pre event) {
    if (event.getPacket() instanceof CPacketCustomPayload) {
      String channel = ((CPacketCustomPayload) event.getPacket()).getChannelName();
      PacketBuffer packetBuffer = ((CPacketCustomPayload) event.getPacket()).getBufferData();
      if (isBlockedPacket(channel, packetBuffer)) event.setCanceled(true);
    }
  }
}
