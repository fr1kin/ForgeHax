package dev.fiki.forgehax.main.mods.chat;

import dev.fiki.forgehax.api.cmd.settings.StringSetting;
import dev.fiki.forgehax.api.mapper.FieldMapping;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.getDisplayScreen;
import static dev.fiki.forgehax.main.Common.getLogger;

@RegisterMod(
    name = "InstantMessage",
    description = "Send message as soon as you join",
    category = Category.CHAT
)
@RequiredArgsConstructor
public class InstantMessage extends ToggleMod {
  @FieldMapping(parentClass = ConnectingScreen.class, value = "networkManager")
  private final ReflectionField<NetworkManager> ConnectingScreen_networkManager;

  private final StringSetting message = newStringSetting()
      .name("message")
      .description("Message to send")
      .defaultTo("Never fear on {SRVNAME}, {NAME} is here!")
      .build();

  @SubscribeEvent
  public void onPacketIn(PacketInboundEvent event) {
    if (event.getPacket() instanceof SLoginSuccessPacket) {
      if (getDisplayScreen() instanceof ConnectingScreen) {
        ServerData serverData = MC.getCurrentServerData();
        String serverName = serverData != null ? serverData.serverName : "Unknown";
        String serverIP = serverData != null ? serverData.serverIP : "";

        ConnectingScreen_networkManager.get(getDisplayScreen()).sendPacket(new CChatMessagePacket(
            message.getValue()
                .replace("{SRVNAME}", serverName)
                .replace("{IP}", serverIP)
                .replace("{NAME}", MC.getSession().getUsername())));
      } else {
        getLogger().warn("Did not send message as current screen is not GuiConnecting");
      }
    }
  }
}
