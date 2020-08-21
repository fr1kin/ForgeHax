package dev.fiki.forgehax.main.mods.misc;

import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.types.ReflectionField;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.util.SharedConstants;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "ProtocolSpoofer",
    description = "Spoof minecraft protocol version",
    category = Category.MISC,
    flags = EnumFlag.HIDDEN
)
@RequiredArgsConstructor
public class ProtocolSpoofer extends ToggleMod {
  @FieldMapping(parentClass = CHandshakePacket.class, value = "protocolVersion")
  private final ReflectionField<Integer> CKeepAlivePacket_protocolVersion;

  private final IntegerSetting version = newIntegerSetting()
      .name("version")
      .description("Spoofed protocol version to use. https://minecraft.gamepedia.com/Protocol_version#Java_Edition")
      .defaultTo(SharedConstants.getVersion().getProtocolVersion())
      .build();

  @SubscribeEvent
  public void onPacketEvent(PacketOutboundEvent event) {
    if (event.getPacket() instanceof CHandshakePacket) {
      CKeepAlivePacket_protocolVersion.set(event.getPacket(), version.intValue());
      getLog().info("Spoofing game version, may cause crashes or instability");
    }
  }
}
