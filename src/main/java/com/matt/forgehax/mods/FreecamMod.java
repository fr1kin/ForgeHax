package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getModManager;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.Switch.Handle;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.key.Bindings;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CInputPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Created on 9/3/2016 by fr1kin */
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
    if (getLocalPlayer() == null || getWorld() == null) return;

    if (isRidingEntity = getLocalPlayer().getRidingEntity() != null) {
      ridingEntity = getLocalPlayer().getRidingEntity();
      getLocalPlayer().dismountEntity(getLocalPlayer().getRidingEntity());
    } else pos = getLocalPlayer().getPositionVector();

    angle = LocalPlayerUtils.getViewAngles();


    originalPlayer = new RemoteClientPlayerEntity(getWorld(), MC.getSession().getProfile());
    originalPlayer.copyLocationAndAnglesFrom(getLocalPlayer());
    originalPlayer.rotationYawHead = getLocalPlayer().rotationYawHead;
    //originalPlayer.inventory = getLocalPlayer().inventory; // 1.14: changed to final
    //originalPlayer.container = getLocalPlayer().container;
    getWorld().func_217411_a(-100, originalPlayer);
    //getWorld().addEntityToWorld(-100, originalPlayer);
  }

  @Override
  public void onDisabled() {
    flying.disable();

    if (getLocalPlayer() == null || originalPlayer == null) return;

    getLocalPlayer().setPositionAndRotation(pos.x, pos.y, pos.z, angle.getYaw(), angle.getPitch());
    getWorld().removeEntityFromWorld(-100);
    originalPlayer = null;

    getLocalPlayer().playerAbilities.isFlying =
        getModManager().get(ElytraFlight.class).map(BaseMod::isEnabled).orElse(false);
    getLocalPlayer().playerAbilities.setFlySpeed(0.05f);
    getLocalPlayer().noClip = false;
    getLocalPlayer().setVelocity(0, 0, 0);

    if (isRidingEntity) {
      getLocalPlayer().startRiding(ridingEntity, true);
      ridingEntity = null;
    }
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (getLocalPlayer() == null) return;

      flying.enable();
    getLocalPlayer().playerAbilities.allowFlying = true;
    getLocalPlayer().playerAbilities.isFlying = true;
    getLocalPlayer().playerAbilities.setFlySpeed(speed.getAsFloat());
    getLocalPlayer().noClip = true;
    getLocalPlayer().onGround = false;
    getLocalPlayer().fallDistance = 0;

    // this is pozzed
    /*if (!Bindings.forward.isPressed()
        && !Bindings.back.isPressed()
        && !Bindings.left.isPressed()
        && !Bindings.right.isPressed()
        && !Bindings.jump.isPressed()
        && !Bindings.sneak.isPressed()) {
      getLocalPlayer().setVelocity(0, 0, 0);
    }*/
  }

  @SubscribeEvent
  public void onPacketSend(PacketEvent.Outgoing.Pre event) {
    if (event.getPacket() instanceof CPlayerPacket || event.getPacket() instanceof CInputPacket) {
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void onPacketReceived(PacketEvent.Incoming.Pre event) {
    if (originalPlayer == null || getLocalPlayer() == null) return;

    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      SPlayerPositionLookPacket packet = event.getPacket();
      pos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
      angle = Angle.degrees(packet.getPitch(), packet.getYaw());
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load event) {
    if (originalPlayer == null || getLocalPlayer() == null) return;

    pos = getLocalPlayer().getPositionVector();
    angle = LocalPlayerUtils.getViewAngles();
  }

  @SubscribeEvent
  public void onEntityRender(RenderLivingEvent.Pre<?, ?> event) {
    if (originalPlayer != null
        && getLocalPlayer() != null
        && getLocalPlayer().equals(event.getEntity())) event.setCanceled(true);
  }

  @SubscribeEvent
  public void onRenderTag(RenderLivingEvent.Specials.Pre event) {
    if (originalPlayer != null
        && getLocalPlayer() != null
        && getLocalPlayer().equals(event.getEntity())) event.setCanceled(true);
  }

  /*private static class DummyPlayer extends RemoteClientPlayerEntity {
    public DummyPlayer(World worldIn) {
      super(worldIn, getLocalPlayer().getGameProfile());
    }
    public DummyPlayer(World worldIn, GameProfile gameProfileIn) {
      super(worldIn, gameProfileIn);
    }

    @Override
    public void tick() {}

    @Override
    public void livingTick() {}
  }*/
}
