package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.Switch.Handle;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
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
  }

  @SubscribeEvent
  public void onPacketSending(PacketEvent.Outgoing.Pre event) {
    EntityPlayer player = getLocalPlayer();
    if (isNull(player)) return;

    if (!groundSpoof.get() || !(event.getPacket() instanceof CPacketPlayer) || !player.capabilities.isFlying)
      return;

    CPacketPlayer packet = event.getPacket();

    if (!(boolean) ObfuscationReflectionHelper.getPrivateValue(CPacketPlayer.class, packet, "moving", "field_149480_h", "g"))
      return;

    AxisAlignedBB range = player.getEntityBoundingBox().expand(0, -player.posY, 0).contract(0, -player.height, 0);
    List<AxisAlignedBB> collisionBoxes = player.world.getCollisionBoxes(player, range);
    AtomicReference<Double> newHeight = new AtomicReference<>(0D);
    collisionBoxes.forEach(box -> newHeight.set(Math.max(newHeight.get(), box.maxY)));

    ObfuscationReflectionHelper.setPrivateValue(CPacketPlayer.class, packet, newHeight.get(), "y", "field_149477_b", "b");
    ObfuscationReflectionHelper.setPrivateValue(CPacketPlayer.class, packet, true, "onGround", "field_149474_g", "f");
  }
}
