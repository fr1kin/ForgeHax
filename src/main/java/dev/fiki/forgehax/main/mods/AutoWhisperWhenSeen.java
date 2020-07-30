package dev.fiki.forgehax.main.mods;

import com.mojang.authlib.GameProfile;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.settings.StringSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.network.play.server.SSpawnPlayerPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;
import java.util.UUID;

@RegisterMod
public class AutoWhisperWhenSeen extends ToggleMod {

  private final StringSetting message = newStringSetting()
      .name("message")
      .description("Message to send")
      .defaultTo("hello uwu")
      .build();

  public AutoWhisperWhenSeen() {
    super(Category.MISC, "AutoWhisperWhenSeen", false, "Automatically send a message to whoever comes into render distance");
  }

  @SubscribeEvent
  public void onPacketReceived(PacketInboundEvent event) {
    if (event.getPacket() instanceof SSpawnPlayerPacket) {
      final SSpawnPlayerPacket packet = (SSpawnPlayerPacket) event.getPacket();
      final UUID id = packet.getUniqueId();
      Optional.ofNullable(Common.MC.getConnection().getPlayerInfo(id))
          .map(NetworkPlayerInfo::getGameProfile)
          .map(GameProfile::getName)
          .ifPresent(name -> {
            Common.getLocalPlayer().sendChatMessage("/w " + name + " " + message.getValue());
          });
    }
  }

}
