package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.Switch.Handle;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.key.Bindings;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
  
  private EntityOtherPlayerMP originalPlayer;
  
  public FreecamMod() {
    super(Category.PLAYER, "Freecam", false, "Freecam mode");
  }
  
  @Override
  public void onEnabled() {
    if (getLocalPlayer() == null || getWorld() == null) {
      return;
    }
    
    if (isRidingEntity = getLocalPlayer().isRiding()) {
      ridingEntity = getLocalPlayer().getRidingEntity();
      getLocalPlayer().dismountRidingEntity();
    } else {
      pos = getLocalPlayer().getPositionVector();
    }
    
    angle = LocalPlayerUtils.getViewAngles();
    
    originalPlayer = new EntityOtherPlayerMP(getWorld(), MC.getSession().getProfile());
    originalPlayer.copyLocationAndAnglesFrom(getLocalPlayer());
    originalPlayer.rotationYawHead = getLocalPlayer().rotationYawHead;
    originalPlayer.inventory = getLocalPlayer().inventory;
    originalPlayer.inventoryContainer = getLocalPlayer().inventoryContainer;
    getWorld().addEntityToWorld(-100, originalPlayer);
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
    getLocalPlayer().capabilities.setFlySpeed(speed.getAsFloat());
    getLocalPlayer().noClip = true;
    getLocalPlayer().onGround = false;
    getLocalPlayer().fallDistance = 0;
    
    if (!Bindings.forward.isPressed()
        && !Bindings.back.isPressed()
        && !Bindings.left.isPressed()
        && !Bindings.right.isPressed()
        && !Bindings.jump.isPressed()
        && !Bindings.sneak.isPressed()) {
      getLocalPlayer().setVelocity(0, 0, 0);
    }
  }
  
  @SubscribeEvent
  public void onPacketSend(PacketEvent.Outgoing.Pre event) {
    if (event.getPacket() instanceof CPacketPlayer || event.getPacket() instanceof CPacketInput) {
      event.setCanceled(true);
    }
  }
  
  @SubscribeEvent
  public void onPacketReceived(PacketEvent.Incoming.Pre event) {
    if (originalPlayer == null || getLocalPlayer() == null) {
      return;
    }
    
    if (event.getPacket() instanceof SPacketPlayerPosLook) {
      SPacketPlayerPosLook packet = event.getPacket();
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
  public void onEntityRender(RenderLivingEvent.Pre<?> event) {
    if (originalPlayer != null
        && getLocalPlayer() != null
        && getLocalPlayer().equals(event.getEntity())) {
      event.setCanceled(true);
    }
  }
  
  @SubscribeEvent
  public void onRenderTag(RenderLivingEvent.Specials.Pre event) {
    if (originalPlayer != null
        && getLocalPlayer() != null
        && getLocalPlayer().equals(event.getEntity())) {
      event.setCanceled(true);
    }
  }
  
  private static class DummyPlayer extends EntityOtherPlayerMP {
    
    public DummyPlayer(World worldIn, GameProfile gameProfileIn) {
      super(worldIn, gameProfileIn);
    }
    
    @Override
    public void onUpdate() {
    }
    
    @Override
    public void onLivingUpdate() {
    }
  }
}
