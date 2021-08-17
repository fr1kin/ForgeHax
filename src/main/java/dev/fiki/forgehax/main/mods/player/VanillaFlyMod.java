package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.Switch.Handle;
import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.FloatSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.Common;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@RegisterMod(
    name = "VanillaFly",
    description = "Fly like creative mode",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class VanillaFlyMod extends ToggleMod {
  private final ReflectionTools common;

  @MapField(parentClass = CPlayerPacket.class, value = "hasPos")
  private final ReflectionField<Boolean> CPacketPlayer_hasPos;

  @MapField(parentClass = SPlayerPositionLookPacket.class, value = "x")
  private final ReflectionField<Double> SPlayerPositionLookPacket_x;

  @MapField(parentClass = SPlayerPositionLookPacket.class, value = "y")
  private final ReflectionField<Double> SPlayerPositionLookPacket_y;

  @MapField(parentClass = SPlayerPositionLookPacket.class, value = "z")
  private final ReflectionField<Double> SPlayerPositionLookPacket_z;

  private Handle fly = LocalPlayerEx.getFlySwitch().createHandle(getName());

  @SuppressWarnings("WeakerAccess")
  public final BooleanSetting groundSpoof = newBooleanSetting()
      .name("spoof")
      .description("make the server think we are on the ground while flying")
      .defaultTo(false)
      .build();

  @SuppressWarnings("WeakerAccess")
  public final BooleanSetting antiGround = newBooleanSetting()
      .name("antiground")
      .description("attempts to prevent the server from teleporting us to the ground")
      .defaultTo(true)
      .build();

  @SuppressWarnings("WeakerAccess")
  public final FloatSetting flySpeed = newFloatSetting()
      .name("speed")
      .description("fly speed as a multiplier of the default")
      .min(0f)
      .defaultTo(1f)
      .build();

  @Override
  protected void onEnabled() {
    fly.enable();
  }

  @Override
  protected void onDisabled() {
    fly.disable();
  }

  @SubscribeListener
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    PlayerEntity player = Common.getLocalPlayer();
    if (isNull(player)) {
      return;
    }

    if (!player.abilities.mayfly) {
      fly.disable();
      fly.enable();
      player.abilities.flying = false;
    }

    player.abilities.setFlyingSpeed(0.05f * flySpeed.getValue());
  }

  @SubscribeListener
  public void onPacketSending(PacketOutboundEvent event) {
    PlayerEntity player = Common.getLocalPlayer();
    if (isNull(player)) {
      return;
    }

    if (!groundSpoof.getValue() || !(event.getPacket() instanceof CPlayerPacket)
        || !player.abilities.flying) {
      return;
    }

    CPlayerPacket packet = (CPlayerPacket) event.getPacket();
    if (!CPacketPlayer_hasPos.get(packet)) {
      return;
    }

    AxisAlignedBB range = player.getBoundingBox().inflate(0, -player.getY(), 0)
        .contract(0, -player.getBbHeight(), 0);
    List<AxisAlignedBB> collisionBoxes = player.level.getBlockCollisions(player, range)
        .map(VoxelShape::bounds)
        .collect(Collectors.toList());
    AtomicReference<Double> newHeight = new AtomicReference<>(0D);
    collisionBoxes.forEach(box -> newHeight.set(Math.max(newHeight.get(), box.maxY)));

    common.CPacketPlayer_y.set(packet, newHeight.get());
    common.CPacketPlayer_onGround.set(packet, true);
  }

  @SubscribeListener
  public void onPacketRecieving(PacketInboundEvent event) {
    PlayerEntity player = Common.getLocalPlayer();
    if (isNull(player)) {
      return;
    }

    if (!antiGround.getValue() || !(event.getPacket() instanceof SPlayerPositionLookPacket)
        || !player.abilities.flying) {
      return;
    }

    SPlayerPositionLookPacket packet = (SPlayerPositionLookPacket) event.getPacket();

    double oldY = player.getY();
    player.moveTo(
        SPlayerPositionLookPacket_x.get(packet),
        SPlayerPositionLookPacket_y.get(packet),
        SPlayerPositionLookPacket_z.get(packet)
    );

    /*
     * This needs a little explanation, as I had a little trouble wrapping my head around it myself.
     * Basically, we're trying to find a new position that's as close to the original height as possible.
     * That way, if you're, for example, spoofing and the server rubberbands you back, you don't go back to the ground.
     * This tries to find the lowest block above the spot the server teleported you to, and teleport you right to that.
     * If the lowest block is below where you were before, it just teleports you to where you were before.
     * This allows VanillaFly to be slightly more usable on servers like Constantiam that like to teleport you in place
     * to hopefully disable fly hacks. Well, sorry guys, this fly hack is smarter than that.
     */
    AxisAlignedBB range = player.getBoundingBox()
        .inflate(0, 256 - player.getHealth() - player.getY(), 0).contract(0, player.getBbHeight(), 0);
    List<AxisAlignedBB> collisionBoxes = player.level.getBlockCollisions(player, range)
        .map(VoxelShape::bounds)
        .collect(Collectors.toList());
    AtomicReference<Double> newY = new AtomicReference<>(256D);
    collisionBoxes.forEach(box -> newY.set(Math.min(newY.get(), box.minY - player.getBbHeight())));

    SPlayerPositionLookPacket_y.set(packet, Math.min(oldY, newY.get()));
  }
}
