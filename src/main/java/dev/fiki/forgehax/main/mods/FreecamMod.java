package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.common.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.cmd.settings.FloatSetting;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.math.Angle;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.Switch.Handle;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CInputPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.*;
import static dev.fiki.forgehax.main.Common.getGameSettings;

/**
 * Created on 9/3/2016 by fr1kin
 */
@RegisterMod
public class FreecamMod extends ToggleMod {

  private final FloatSetting speed = newFloatSetting()
      .name("speed")
      .description("Movement speed")
      .defaultTo(0.05f)
      .build();

  private final Handle flying = LocalPlayerUtils.getFlySwitch().createHandle(getName());

  private Vec3d pos = Vec3d.ZERO;
  private Angle angle = Angle.ZERO;

  private boolean isRidingEntity;
  private Entity ridingEntity;

  private RemoteClientPlayerEntity originalPlayer;

  public FreecamMod() {
    super(Category.PLAYER, "Freecam", false, "Freecam mode");
  }

  @Override
  public void onEnabled() {
    if (!isInWorld()) {
      return;
    }

    if (isRidingEntity = getLocalPlayer().getRidingEntity() != null) {
      ridingEntity = getLocalPlayer().getRidingEntity();
      getLocalPlayer().stopRiding();
    } else {
      pos = getLocalPlayer().getPositionVector();
    }

    angle = LocalPlayerUtils.getViewAngles();

    originalPlayer = new RemoteClientPlayerEntity(getWorld(), MC.getSession().getProfile());
    originalPlayer.copyLocationAndAnglesFrom(getLocalPlayer());
    originalPlayer.rotationYawHead = getLocalPlayer().rotationYawHead;
    //originalPlayer.inventory = getLocalPlayer().inventory;
    //originalPlayer.container = getLocalPlayer().container;

    getWorld().addEntity(-100, originalPlayer);
  }

  @Override
  public void onDisabled() {
    flying.disable();

    if (getLocalPlayer() == null || originalPlayer == null) {
      return;
    }

    getLocalPlayer().setPositionAndRotation(pos.x, pos.y, pos.z, angle.getYaw(), angle.getPitch());
    getWorld().removeEntityFromWorld(-100);
    originalPlayer = null;

    getLocalPlayer().noClip = false;
    getLocalPlayer().setVelocity(0, 0, 0);

    if (isRidingEntity) {
      getLocalPlayer().startRiding(ridingEntity, true);
      ridingEntity = null;
    }
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (getLocalPlayer() == null) {
      return;
    }

    flying.enable();
    getLocalPlayer().abilities.setFlySpeed(speed.getValue());
    getLocalPlayer().noClip = true;
    getLocalPlayer().onGround = false;
    getLocalPlayer().fallDistance = 0;

    if (!getGameSettings().keyBindForward.isPressed()
        && !getGameSettings().keyBindBack.isPressed()
        && !getGameSettings().keyBindLeft.isPressed()
        && !getGameSettings().keyBindRight.isPressed()
        && !getGameSettings().keyBindJump.isPressed()
        && !getGameSettings().keyBindSneak.isPressed()) {
      getLocalPlayer().setVelocity(0, 0, 0);
    }
  }

  @SubscribeEvent
  public void onPacketSend(PacketOutboundEvent event) {
    if (event.getPacket() instanceof CPlayerPacket || event.getPacket() instanceof CInputPacket) {
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void onPacketReceived(PacketInboundEvent event) {
    if (originalPlayer == null || getLocalPlayer() == null) {
      return;
    }

    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      SPlayerPositionLookPacket packet = (SPlayerPositionLookPacket) event.getPacket();
      pos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
      angle = Angle.degrees(packet.getPitch(), packet.getYaw());
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load event) {
    if (originalPlayer == null || getLocalPlayer() == null) {
      return;
    }

    pos = getLocalPlayer().getPositionVector();
    angle = LocalPlayerUtils.getViewAngles();
  }

  @SubscribeEvent
  public void onEntityRender(RenderLivingEvent.Pre<?, ?> event) {
    if (originalPlayer != null
        && getLocalPlayer() != null
        && getLocalPlayer().equals(event.getEntity())) {
      event.setCanceled(true);
    }
  }

//  @SubscribeEvent
//  public void onRenderTag(RenderLivingEvent.Specials.Pre event) {
//    if (originalPlayer != null
//        && getLocalPlayer() != null
//        && getLocalPlayer().equals(event.getEntity())) {
//      event.setCanceled(true);
//    }
//  } // TODO: 1.15 disable nametag
}
