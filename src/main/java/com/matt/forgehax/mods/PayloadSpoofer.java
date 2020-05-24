package com.matt.forgehax.mods;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import io.netty.buffer.ByteBuf;
import java.util.Scanner;
import java.util.Set;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

/**
 * Created on 6/2/2017 by fr1kin
 */
@RegisterMod
public class PayloadSpoofer extends ToggleMod {
  
  private static final Set<String> IGNORE_LIST = Sets.newHashSet();
  
  static {
    // https://github.com/Pokechu22/WorldDownloader/commit/b4bd489b0117a3bff3d56851ed8823cb252d81e0
    // old builds
    IGNORE_LIST.add("WDL|INIT");
    IGNORE_LIST.add("WDL|CONTROL");
    IGNORE_LIST.add("WDL|REQUEST");
    // new builds even for 1.12 liteloader
    IGNORE_LIST.add("wdl:init");
    IGNORE_LIST.add("wdl:control");
    IGNORE_LIST.add("wdl:request");
    // FML Proxy packets:
    IGNORE_LIST.add("schematica"); // https://www.spigotmc.org/resources/schematicaplugin.14411/
    IGNORE_LIST.add("journeymap_channel"); // doesnt appear to respect old chat MOTD method (see NCP Permissions)
    IGNORE_LIST.add("jm_dim_permission");
    IGNORE_LIST.add("jm_init_login");
  }
  
  public PayloadSpoofer() {
    super(Category.MISC, "PayloadSpoofer", false, "Will cancel packets sent by some mods");
  }
  
  private boolean isBlockedPacket(String channel, ByteBuf buffer) {
    if (IGNORE_LIST.contains(channel)) {
      return true;
    } else if ("REGISTER".equals(channel)) {
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
  public void onIncomingPacket(PacketEvent.Incoming.Pre event) {
    String channel;
    ByteBuf packetBuffer;
    final Packet packet = event.getPacket();
  
    if (packet instanceof SPacketCustomPayload || packet instanceof FMLProxyPacket) {
      if (packet instanceof SPacketCustomPayload) {
        channel = ((SPacketCustomPayload) packet).getChannelName();
        packetBuffer = ((SPacketCustomPayload) packet).getBufferData();
      } else {
        channel = ((FMLProxyPacket) packet).channel();
        packetBuffer = ((FMLProxyPacket) packet).payload();
      }
      
      if (isBlockedPacket(channel, packetBuffer)) {
        event.setCanceled(true);
      }
    }
  }
  
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onOutgoingPacket(PacketEvent.Outgoing.Pre event) {
    String channel;
    ByteBuf packetBuffer;
    final Packet packet = event.getPacket();
    
    if (packet instanceof CPacketCustomPayload || packet instanceof FMLProxyPacket) {
      if (packet instanceof CPacketCustomPayload) {
        channel = ((CPacketCustomPayload) packet).getChannelName();
        packetBuffer = ((CPacketCustomPayload) packet).getBufferData();
      } else {
        channel = ((FMLProxyPacket) packet).channel();
        packetBuffer = ((FMLProxyPacket) packet).payload();
      }
      
      if (isBlockedPacket(channel, packetBuffer)) {
        event.setCanceled(true);
      }
    }
  }
}
