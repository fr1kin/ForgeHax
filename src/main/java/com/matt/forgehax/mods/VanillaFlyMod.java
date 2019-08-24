package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection.Fields;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.Switch.Handle;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.util.entity.LocalPlayerUtils.getFlySwitch;
import static java.util.Objects.isNull;

@RegisterMod
public class VanillaFlyMod extends ToggleMod {
  private Handle fly = getFlySwitch().createHandle(getModName());

  @SuppressWarnings("WeakerAccess")
  public final Setting<Boolean> groundSpoof =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("spoof")
          .description("make the server think we are on the ground while flying")
          .defaultTo(false)
          .build();

  @SuppressWarnings("WeakerAccess")
  public final Setting<Boolean> antiGround =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("antiground")
          .description("attempts to prevent the server from teleporting us to the ground")
          .defaultTo(true)
          .build();

  @SuppressWarnings("WeakerAccess")
  public final Setting<Float> flySpeed =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
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
    EntityPlayer player = getLocalPlayer();
    if (isNull(player)) return;

    if (!player.capabilities.allowFlying) {
      fly.disable();
      fly.enable();
      player.capabilities.isFlying = false;
    }

    player.capabilities.setFlySpeed(0.05f * flySpeed.get());
  }

  @SubscribeEvent
  public void onPacketSending(PacketEvent.Outgoing.Pre event) {
    EntityPlayer player = getLocalPlayer();
    if (isNull(player)) return;

    if (!groundSpoof.get() || !(event.getPacket() instanceof CPacketPlayer) || !player.capabilities.isFlying)
      return;

    CPacketPlayer packet = event.getPacket();
    if (!Fields.CPacketPlayer_moving.get(packet)) return;

    AxisAlignedBB range = player.getEntityBoundingBox().expand(0, -player.posY, 0).contract(0, -player.height, 0);
    List<AxisAlignedBB> collisionBoxes = player.world.getCollisionBoxes(player, range);
    AtomicReference<Double> newHeight = new AtomicReference<>(0D);
    collisionBoxes.forEach(box -> newHeight.set(Math.max(newHeight.get(), box.maxY)));

    Fields.CPacketPlayer_y.set(packet, newHeight.get());
    Fields.CPacketPlayer_onGround.set(packet, true);
  }

  @SubscribeEvent
  public void onPacketRecieving(PacketEvent.Incoming.Pre event) {
    EntityPlayer player = getLocalPlayer();
    if (isNull(player)) return;

    if (!antiGround.get() || !(event.getPacket() instanceof SPacketPlayerPosLook) || !player.capabilities.isFlying)
      return;

    SPacketPlayerPosLook packet = event.getPacket();

    double oldY = player.posY;
    player.setPosition(
        Fields.SPacketPlayer_x.get(packet),
        Fields.SPacketPlayer_y.get(packet),
        Fields.SPacketPlayer_z.get(packet)
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
    AxisAlignedBB range = player.getEntityBoundingBox().expand(0, 256 - player.height - player.posY, 0).contract(0, player.height, 0);
    List<AxisAlignedBB> collisionBoxes = player.world.getCollisionBoxes(player, range);
    AtomicReference<Double> newY = new AtomicReference<>(256D);
    collisionBoxes.forEach(box -> newY.set(Math.min(newY.get(), box.minY - player.height)));

    Fields.SPacketPlayer_y.set(packet, Math.min(oldY, newY.get()));
  }
}
