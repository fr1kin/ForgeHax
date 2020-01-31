package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.network.play.server.SSpawnPlayerPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;
import java.util.UUID;

@RegisterMod
// TODO: rename this
public class AutoWhisperWhenSeen extends ToggleMod {

  private final Setting<String> message =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("message")
          .description("Message to send")
          .defaultTo("hello uwu")
          .build();

  public AutoWhisperWhenSeen() {
    super(Category.MISC, "ThisModIsDedicatedToUfoCrossing", false, "Automatically send a message to whoever comes into render distance");
  }

  @SubscribeEvent
  public void onPacketReceived(PacketInboundEvent event) {
    if (event.getPacket() instanceof SSpawnPlayerPacket) {
      final SSpawnPlayerPacket packet = (SSpawnPlayerPacket) event.getPacket();
      final UUID id = packet.getUniqueId();
      Optional.ofNullable(Globals.MC.getConnection().getPlayerInfo(id))
        .map(NetworkPlayerInfo::getGameProfile)
        .map(GameProfile::getName)
        .ifPresent(name -> {
          Globals.getLocalPlayer().sendChatMessage("/w " + name + " " + message.get());
        });
    }
  }

}
