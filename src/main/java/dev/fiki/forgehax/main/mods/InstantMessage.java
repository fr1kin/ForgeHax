package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.settings.StringSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.login.server.SLoginSuccessPacket;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class InstantMessage extends ToggleMod {

  private final StringSetting message = newStringSetting()
      .name("message")
      .description("Message to send")
      .defaultTo("Never fear on {SRVNAME}, {NAME} is here!")
      .build();

  public InstantMessage() {
    super(Category.MISC, "InstantMessage", false, "Send message as soon as you join");
  }

  @SubscribeEvent
  public void onPacketIn(PacketInboundEvent event) {
    if (event.getPacket() instanceof SLoginSuccessPacket) {
      if (Common.getDisplayScreen() instanceof ConnectingScreen) {
        ServerData serverData = Common.MC.getCurrentServerData();
        String serverName = serverData != null ? serverData.serverName : "Unknown";
        String serverIP = serverData != null ? serverData.serverIP : "";

        FastReflection.Fields.ConnectingScreen_networkManager.get(Common.MC.currentScreen)
            .sendPacket(
                new CChatMessagePacket(
                    message
                        .getValue()
                        .replace("{SRVNAME}", serverName)
                        .replace("{IP}", serverIP)
                        .replace("{NAME}", Common.MC.getSession().getUsername())));
      } else {
        Common.getLogger().warn("Did not send message as current screen is not GuiConnecting");
      }
    }
  }
}
