package com.matt.forgehax.mods;

import com.matt.forgehax.Globals;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.network.play.server.SSpawnPlayerPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;
import java.util.UUID;

import static com.matt.forgehax.Globals.*;

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
  public void onPacketReceived(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SSpawnPlayerPacket) {
      final SSpawnPlayerPacket packet = event.getPacket();
      final UUID id = packet.getUniqueId();
      Optional.ofNullable(MC.getConnection().getPlayerInfo(id))
        .map(NetworkPlayerInfo::getGameProfile)
        .map(GameProfile::getName)
        .ifPresent(name -> {
          getLocalPlayer().sendChatMessage("/w " + name + " " + message.get());
        });
    }
  }

}
