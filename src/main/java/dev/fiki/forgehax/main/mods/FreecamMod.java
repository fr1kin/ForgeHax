package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.common.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.key.Bindings;
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

/**
 * Created on 9/3/2016 by fr1kin
 */
@RegisterMod
public class FreecamMod extends ToggleMod {
  
  private final Setting<Double> speed =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("speed")
          .description("Movement speed")
          .defaultTo(0.05D)
          .build();
  
  private final Handle flying = LocalPlayerUtils.getFlySwitch().createHandle(getModName());
  
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
    if (!Globals.isInWorld()) {
      return;
    }
    
    if (isRidingEntity = Globals.getLocalPlayer().getRidingEntity() != null) {
      ridingEntity = Globals.getLocalPlayer().getRidingEntity();
      Globals.getLocalPlayer().stopRiding();
    } else {
      pos = Globals.getLocalPlayer().getPositionVector();
    }
    
    angle = LocalPlayerUtils.getViewAngles();

    originalPlayer = new RemoteClientPlayerEntity(Globals.getWorld(), Globals.MC.getSession().getProfile());
    originalPlayer.copyLocationAndAnglesFrom(Globals.getLocalPlayer());
    originalPlayer.rotationYawHead = Globals.getLocalPlayer().rotationYawHead;
    //originalPlayer.inventory = getLocalPlayer().inventory;
    //originalPlayer.container = getLocalPlayer().container;

    Globals.getWorld().addEntity(-100, originalPlayer);
  }
  
  @Override
  public void onDisabled() {
    flying.disable();
    
    if (Globals.getLocalPlayer() == null || originalPlayer == null) {
      return;
    }
    
    Globals.getLocalPlayer().setPositionAndRotation(pos.x, pos.y, pos.z, angle.getYaw(), angle.getPitch());
    Globals.getWorld().removeEntityFromWorld(-100);
    originalPlayer = null;
    
    Globals.getLocalPlayer().noClip = false;
    Globals.getLocalPlayer().setVelocity(0, 0, 0);
    
    if (isRidingEntity) {
      Globals.getLocalPlayer().startRiding(ridingEntity, true);
      ridingEntity = null;
    }
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (Globals.getLocalPlayer() == null) {
      return;
    }
    
    flying.enable();
    Globals.getLocalPlayer().abilities.setFlySpeed(speed.getAsFloat());
    Globals.getLocalPlayer().noClip = true;
    Globals.getLocalPlayer().onGround = false;
    Globals.getLocalPlayer().fallDistance = 0;
    
    if (!Bindings.forward.isPressed()
        && !Bindings.back.isPressed()
        && !Bindings.left.isPressed()
        && !Bindings.right.isPressed()
        && !Bindings.jump.isPressed()
        && !Bindings.sneak.isPressed()) {
      Globals.getLocalPlayer().setVelocity(0, 0, 0);
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
    if (originalPlayer == null || Globals.getLocalPlayer() == null) {
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
    if (originalPlayer == null || Globals.getLocalPlayer() == null) {
      return;
    }
    
    pos = Globals.getLocalPlayer().getPositionVector();
    angle = LocalPlayerUtils.getViewAngles();
  }
  
  @SubscribeEvent
  public void onEntityRender(RenderLivingEvent.Pre<?, ?> event) {
    if (originalPlayer != null
        && Globals.getLocalPlayer() != null
        && Globals.getLocalPlayer().equals(event.getEntity())) {
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
