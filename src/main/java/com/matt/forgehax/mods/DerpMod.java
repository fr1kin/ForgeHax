package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.PacketHelper;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class DerpMod extends ToggleMod {
  public DerpMod() {
    super(Category.MISC, "Derp", false, "Derp");
  }

  enum DerpMode {
    UPSIDE_DOWN
  }

  private final Setting<DerpMode> mode =
      getCommandStub()
          .builders()
          .<DerpMode>newSettingEnumBuilder()
          .name("mode")
          .description("Derp mode")
          .defaultTo(DerpMode.UPSIDE_DOWN)
          .build();

  private final Setting<Float> randomSpeed =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("speed")
          .description("Random derp speed")
          .defaultTo(1f)
          .build();

  @SubscribeEvent
  public void onPacketSending(PacketEvent.Outgoing.Pre event) {
    if (mode.get() != DerpMode.UPSIDE_DOWN) return;
    if (PacketHelper.isIgnored(event.getPacket()) || !(event.getPacket() instanceof CPacketPlayer)) return;

    CPacketPlayer packet = event.getPacket();
    boolean isRotation = packet instanceof CPacketPlayer.Rotation || packet instanceof CPacketPlayer.PositionRotation;

    if (isRotation) {
      float newPitch = 360;
      float newYaw = packet.getYaw(0) + 360;
      boolean onGround = packet.isOnGround();
      Packet<?> toSend;

      if (packet instanceof CPacketPlayer.PositionRotation) {
        double x = packet.getX(0);
        double y = packet.getY(0);
        double z = packet.getZ(0);

        toSend = new CPacketPlayer.PositionRotation(x, y, z, newYaw, newPitch, onGround);
      } else {
        toSend = new CPacketPlayer.Rotation(newYaw, newPitch, onGround);
      }

      PacketHelper.ignoreAndSend(toSend);
      event.setCanceled(true);
    }
  }
}
