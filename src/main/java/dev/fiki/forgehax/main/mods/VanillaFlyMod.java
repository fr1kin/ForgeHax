package dev.fiki.forgehax.main.mods;

import static java.util.Objects.isNull;

import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.common.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.FloatSetting;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.Switch.Handle;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class VanillaFlyMod extends ToggleMod {

  private Handle fly = LocalPlayerUtils.getFlySwitch().createHandle(getName());

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

  public VanillaFlyMod() {
    super(Category.PLAYER, "VanillaFly", false, "Fly like creative mode");
  }

  @Override
  protected void onEnabled() {
    fly.enable();
  }

  @Override
  protected void onDisabled() {
    fly.disable();
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    PlayerEntity player = Common.getLocalPlayer();
    if (isNull(player)) {
      return;
    }

    if (!player.abilities.allowFlying) {
      fly.disable();
      fly.enable();
      player.abilities.isFlying = false;
    }

    player.abilities.setFlySpeed(0.05f * flySpeed.getValue());
  }

  @SubscribeEvent
  public void onPacketSending(PacketOutboundEvent event) {
    PlayerEntity player = Common.getLocalPlayer();
    if (isNull(player)) {
      return;
    }

    if (!groundSpoof.getValue() || !(event.getPacket() instanceof CPlayerPacket)
        || !player.abilities.isFlying) {
      return;
    }

    CPlayerPacket packet = (CPlayerPacket) event.getPacket();
    if (!FastReflection.Fields.CPacketPlayer_moving.get(packet)) {
      return;
    }

    AxisAlignedBB range = player.getBoundingBox().expand(0, -player.getPosY(), 0)
        .contract(0, -player.getHeight(), 0);
    List<AxisAlignedBB> collisionBoxes = player.world.getEmptyCollisionShapes(player, range, Collections.emptySet())
        .map(VoxelShape::getBoundingBox)
        .collect(Collectors.toList());
    AtomicReference<Double> newHeight = new AtomicReference<>(0D);
    collisionBoxes.forEach(box -> newHeight.set(Math.max(newHeight.get(), box.maxY)));

    FastReflection.Fields.CPacketPlayer_y.set(packet, newHeight.get());
    FastReflection.Fields.CPacketPlayer_onGround.set(packet, true);
  }

  @SubscribeEvent
  public void onPacketRecieving(PacketInboundEvent event) {
    PlayerEntity player = Common.getLocalPlayer();
    if (isNull(player)) {
      return;
    }

    if (!antiGround.getValue() || !(event.getPacket() instanceof SPlayerPositionLookPacket)
        || !player.abilities.isFlying) {
      return;
    }

    SPlayerPositionLookPacket packet = (SPlayerPositionLookPacket) event.getPacket();

    double oldY = player.getPosY();
    player.setPosition(
        FastReflection.Fields.SPlayerPositionLookPacket_x.get(packet),
        FastReflection.Fields.SPlayerPositionLookPacket_y.get(packet),
        FastReflection.Fields.SPlayerPositionLookPacket_z.get(packet)
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
        .expand(0, 256 - player.getHealth() - player.getPosY(), 0).contract(0, player.getHeight(), 0);
    List<AxisAlignedBB> collisionBoxes = player.world.getEmptyCollisionShapes(player, range, Collections.emptySet())
        .map(VoxelShape::getBoundingBox)
        .collect(Collectors.toList());
    AtomicReference<Double> newY = new AtomicReference<>(256D);
    collisionBoxes.forEach(box -> newY.set(Math.min(newY.get(), box.minY - player.getHeight())));

    FastReflection.Fields.SPlayerPositionLookPacket_y.set(packet, Math.min(oldY, newY.get()));
  }
}
