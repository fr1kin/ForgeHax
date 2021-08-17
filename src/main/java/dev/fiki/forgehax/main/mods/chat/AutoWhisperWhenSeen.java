package dev.fiki.forgehax.main.mods.chat;

import com.mojang.authlib.GameProfile;
import dev.fiki.forgehax.api.cmd.settings.StringSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.network.play.server.SSpawnPlayerPacket;

import java.util.Optional;
import java.util.UUID;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "AutoWhisperWhenSeen",
    description = "Automatically send a message to whoever comes into render distance",
    category = Category.CHAT
)
public class AutoWhisperWhenSeen extends ToggleMod {
  private final StringSetting message = newStringSetting()
      .name("message")
      .description("Message to send")
      .defaultTo("hello uwu")
      .build();

  @SubscribeListener
  public void onPacketReceived(PacketInboundEvent event) {
    if (event.getPacket() instanceof SSpawnPlayerPacket) {
      final SSpawnPlayerPacket packet = (SSpawnPlayerPacket) event.getPacket();
      final UUID id = packet.getPlayerId();
      Optional.ofNullable(MC.getConnection().getPlayerInfo(id))
          .map(NetworkPlayerInfo::getProfile)
          .map(GameProfile::getName)
          .ifPresent(name -> {
            getLocalPlayer().chat("/w " + name + " " + message.getValue());
          });
    }
  }

}
