package dev.fiki.forgehax.main.mods.misc;

import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.util.SharedConstants;

@RegisterMod(
    name = "ProtocolSpoofer",
    description = "Spoof minecraft protocol version",
    category = Category.MISC,
    flags = EnumFlag.HIDDEN
)
@RequiredArgsConstructor
public class ProtocolSpoofer extends ToggleMod {
  @MapField(parentClass = CHandshakePacket.class, value = "protocolVersion")
  private final ReflectionField<Integer> CKeepAlivePacket_protocolVersion;

  private final IntegerSetting version = newIntegerSetting()
      .name("version")
      .description("Spoofed protocol version to use. https://minecraft.gamepedia.com/Protocol_version#Java_Edition")
      .defaultTo(SharedConstants.getCurrentVersion().getProtocolVersion())
      .build();

  @SubscribeListener
  public void onPacketEvent(PacketOutboundEvent event) {
    if (event.getPacket() instanceof CHandshakePacket) {
      CKeepAlivePacket_protocolVersion.set(event.getPacket(), version.intValue());
      getLog().info("Spoofing game version, may cause crashes or instability");
    }
  }
}
