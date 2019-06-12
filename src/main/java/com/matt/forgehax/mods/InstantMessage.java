package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLog;
import static com.matt.forgehax.asm.reflection.FastReflection.Fields.GuiConnecting_networkManager;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.login.server.SLoginSuccessPacket;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class InstantMessage extends ToggleMod {
  private final Setting<String> message =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("message")
          .description("Message to send")
          .defaultTo("test")
          .build();

  public InstantMessage() {
    super(Category.MISC, "InstantMessage", false, "Send message as soon as you join");
  }

  @SubscribeEvent
  public void onPacketIn(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SLoginSuccessPacket) {

      if (MC.field_71462_r instanceof ConnectingScreen) {

        ServerData serverData = MC.getCurrentServerData();
        String serverName = serverData != null ? serverData.serverName : "Unknown";
        String serverIP = serverData != null ? serverData.serverIP : "";

        GuiConnecting_networkManager.get(MC.field_71462_r)
            .sendPacket(
                new CChatMessagePacket(
                    message
                        .get()
                        .replace("{SRVNAME}", serverName)
                        .replace("{IP}", serverIP)
                        .replace("{NAME}", MC.getSession().getUsername())));
      } else getLog().warn("Did not send message as current screen is not GuiConnecting");
    }
  }
}
