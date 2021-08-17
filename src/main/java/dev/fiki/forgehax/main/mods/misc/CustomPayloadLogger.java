package dev.fiki.forgehax.main.mods.misc;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.Common;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@RegisterMod(
    name = "PayloadLogger",
    description = "Logs custom payloads",
    category = Category.MISC
)
public class CustomPayloadLogger extends ToggleMod {
  private static final Path CLIENT_PAYLOAD_LOG =
      Common.getFileManager().getMkBaseResolve("logs/payload/client2server_payload.log");
  private static final Path SERVER_PAYLOAD_LOG =
      Common.getFileManager().getMkBaseResolve("logs/payload/server2client_payload.log");

  private void log(IPacket packet) {
    if (packet instanceof SCustomPayloadPlayPacket) {
      SCustomPayloadPlayPacket payloadPacket = (SCustomPayloadPlayPacket) packet;
      String input =
          String.format(
              "%s=%s\n",
              payloadPacket.getName(), payloadPacket.getData().toString());
      try {
        Files.write(
            SERVER_PAYLOAD_LOG,
            input.getBytes(),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND);
      } catch (Exception e) {
      }
    } else if (packet instanceof CCustomPayloadPacket) {
      CCustomPayloadPacket payloadPacket = (CCustomPayloadPacket) packet;
      String input = String.format("%s=%s\n",
          payloadPacket.getName(), payloadPacket.getInternalData());
      try {
        Files.write(
            CLIENT_PAYLOAD_LOG,
            input.getBytes(),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND);
      } catch (Exception e) {
      }
    }
  }

  @SubscribeListener
  public void onOutgoingCustomPayload(PacketOutboundEvent event) {
    log(event.getPacket());
  }

  @SubscribeListener
  public void onIncomingCustomPayload(PacketInboundEvent event) {
    log(event.getPacket());
  }
}
