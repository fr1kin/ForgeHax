package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getFileManager;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Created on 6/1/2017 by fr1kin */
@RegisterMod
public class CustomPayloadLogger extends ToggleMod {
  private static final Path CLIENT_PAYLOAD_LOG =
      getFileManager().getMkBaseResolve("logs/payload/client2server_payload.log");
  private static final Path SERVER_PAYLOAD_LOG =
      getFileManager().getMkBaseResolve("logs/payload/server2client_payload.log");

  public CustomPayloadLogger() {
    super(Category.MISC, "PayloadLogger", false, "Logs custom payloads");
  }

  private void log(IPacket packet) {
    if (packet instanceof SCustomPayloadPlayPacket) {
      SCustomPayloadPlayPacket payloadPacket = (SCustomPayloadPlayPacket) packet;
      String input =
          String.format(
              "%s=%s\n",
              payloadPacket.getChannelName(), payloadPacket.getBufferData().toString());
      try {
        Files.write(
            SERVER_PAYLOAD_LOG,
            input.getBytes(),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else if (packet instanceof CCustomPayloadPacket) {
      CCustomPayloadPacket payloadPacket = (CCustomPayloadPacket) packet;
      String input =
          String.format(
              "%s=%s\n",
              payloadPacket.getName(), payloadPacket.getData().toString()
          );
      try {
        Files.write(
            CLIENT_PAYLOAD_LOG,
            input.getBytes(),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @SubscribeEvent
  public void onOutgoingCustomPayload(PacketEvent.Outgoing.Pre event) {
    log(event.getPacket());
  }

  @SubscribeEvent
  public void onIncomingCustomPayload(PacketEvent.Incoming.Pre event) {
    log(event.getPacket());
  }
}
