package dev.fiki.forgehax.main.mods.misc;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import dev.fiki.forgehax.api.cmd.argument.Arguments;
import dev.fiki.forgehax.api.cmd.settings.collections.SimpleSettingSet;
import dev.fiki.forgehax.api.common.PriorityEnum;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;

import java.util.Scanner;

@RegisterMod(
    name = "PayloadSpoofer",
    description = "Will cancel packets sent by some mods",
    category = Category.MISC
)
public class PayloadSpoofer extends ToggleMod {
  private final SimpleSettingSet<String> channels = newSimpleSettingSet(String.class)
      .name("channels")
      .description("Payload channels to block")
      .supplier(Sets::newHashSet)
      .argument(Arguments.newStringArgument()
          .label("channel")
          .build())
      .build();

  private boolean isBlockedPacket(String channel, PacketBuffer buffer) {
    if (channels.contains(channel)) {
      return true;
    } else if ("REGISTER".equals(channel)) {
      Scanner scanner = new Scanner(new String(buffer.array()));
      scanner.useDelimiter("\\u0000");
      if (scanner.hasNext()) {
        String next = scanner.next();
        return !Strings.isNullOrEmpty(next) && channels.contains(next);
      }
    }
    return false;
  }

  @SubscribeListener(priority = PriorityEnum.HIGHEST)
  public void onIncomingPacket(PacketInboundEvent event) {
    if (event.getPacket() instanceof SCustomPayloadPlayPacket) {
      String channel = ((SCustomPayloadPlayPacket) event.getPacket()).getName().toString();
      PacketBuffer packetBuffer = ((SCustomPayloadPlayPacket) event.getPacket()).getData();
      if (isBlockedPacket(channel, packetBuffer)) {
        event.setCanceled(true);
      }
    }
  }

  @SubscribeListener(priority = PriorityEnum.HIGHEST)
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
