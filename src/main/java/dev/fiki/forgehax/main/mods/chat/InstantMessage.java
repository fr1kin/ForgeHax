package dev.fiki.forgehax.main.mods.chat;

import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.cmd.settings.StringSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.server.SLoginSuccessPacket;
import net.minecraft.network.play.client.CChatMessagePacket;

import static dev.fiki.forgehax.main.Common.getDisplayScreen;

@RegisterMod(
    name = "InstantMessage",
    description = "Send message as soon as you join",
    category = Category.CHAT
)
@RequiredArgsConstructor
public class InstantMessage extends ToggleMod {
  @MapField(parentClass = ConnectingScreen.class, value = "connection")
  private final ReflectionField<NetworkManager> ConnectingScreen_connection;

  private final StringSetting message = newStringSetting()
      .name("message")
      .description("Message to send")
      .defaultTo("Never fear on {SRVNAME}, {NAME} is here!")
      .build();

  @SubscribeListener
  public void onPacketIn(PacketInboundEvent event) {
    if (event.getPacket() instanceof SLoginSuccessPacket) {
      if (getDisplayScreen() instanceof ConnectingScreen) {
        ServerData serverData = MC.getCurrentServer();
        String serverName = serverData != null ? serverData.name : "Unknown";
        String serverIP = serverData != null ? serverData.ip : "";

        ConnectingScreen_connection.get(getDisplayScreen()).send(new CChatMessagePacket(
            message.getValue()
                .replace("{SRVNAME}", serverName)
                .replace("{IP}", serverIP)
                .replace("{NAME}", MC.getUser().getName())));
      } else {
        log.warn("Did not send message as current screen is not GuiConnecting");
      }
    }
  }
}
