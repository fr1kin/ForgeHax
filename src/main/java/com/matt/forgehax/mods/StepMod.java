package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.PacketHelper;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.*;
import static com.matt.forgehax.Helper.getRidingEntity;

@RegisterMod
public class StepMod extends ToggleMod {
  
  private static final float DEFAULT_STEP_HEIGHT = 0.6f;

  private final Setting<Boolean> entityStep =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("entity-step")
      .description("entitystep")
      .defaultTo(false)
      .build();
  
  private final Setting<Float> stepHeight =
    getCommandStub()
      .builders()
      .<Float>newSettingBuilder()
      .name("height")
      .description("how high you can step")
      .defaultTo(1.2f)
      .min(0f)
      .changed(__ -> MC.addScheduledTask(() -> {
        if (isEnabled()) {
          EntityPlayer player = getLocalPlayer();
          if (player != null) {
            updateStepHeight(player);
          }
        }
      }))
      .build();
  
  private final Setting<Boolean> unstep =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("unstep")
      .description("step down instead of falling")
      .defaultTo(false)
      .build();
  
  public StepMod() {
    super(Category.PLAYER, "Step", false, "Step up blocks");
  }
  
  @Override
  protected void onEnabled() {
    EntityPlayer player = getLocalPlayer();
    if (player != null) {
      wasOnGround = player.onGround;
    }
  }
  
  @Override
  public void onDisabled() {
    EntityPlayer player = getLocalPlayer();
    if (player != null) {
      player.stepHeight = DEFAULT_STEP_HEIGHT;
    }

    if (getRidingEntity() != null) {
      getRidingEntity().stepHeight = 1;
    }
  }
  
  private void updateStepHeight(EntityPlayer player) {
    player.stepHeight = player.onGround ? stepHeight.get() : DEFAULT_STEP_HEIGHT;
  }
  
  private boolean wasOnGround = false;
  
  private void unstep(EntityPlayer player) {
    AxisAlignedBB range = player.getEntityBoundingBox().expand(0, -stepHeight.get(), 0)
      .contract(0, player.height, 0);
    
    if (!player.world.collidesWithAnyBlock(range)) {
      return;
    }
    
    List<AxisAlignedBB> collisionBoxes = player.world.getCollisionBoxes(player, range);
    AtomicReference<Double> newY = new AtomicReference<>(0D);
    collisionBoxes.forEach(box -> newY.set(Math.max(newY.get(), box.maxY)));
    player.setPositionAndUpdate(player.posX, newY.get(), player.posZ);
  }
  
  private void updateUnstep(EntityPlayer player) {
    try {
      if (unstep.get() && wasOnGround && !player.onGround && player.motionY <= 0) {
        unstep(player);
      }
    } finally {
      wasOnGround = player.onGround;
    }
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    EntityPlayer player = (EntityPlayer) event.getEntityLiving();
    if (player == null) {
      return;
    }
    
    updateStepHeight(player);
    updateUnstep(player);

    if (getRidingEntity() != null) {
      if (entityStep.getAsBoolean()) {
        getRidingEntity().stepHeight = 256;
      } else {
        getRidingEntity().stepHeight = 1;
      }
    }
  }
  
  private CPacketPlayer previousPositionPacket = null;
  
  @SubscribeEvent
  public void onPacketSending(PacketEvent.Outgoing.Pre event) {
    if (event.getPacket() instanceof CPacketPlayer.Position
      || event.getPacket() instanceof CPacketPlayer.PositionRotation) {
      CPacketPlayer packetPlayer = (CPacketPlayer) event.getPacket();
      if (previousPositionPacket != null && !PacketHelper.isIgnored(event.getPacket())) {
        double diffY = packetPlayer.getY(0.f) - previousPositionPacket.getY(0.f);
        // y difference must be positive
        // greater than 1, but less than 1.5
        if (diffY > DEFAULT_STEP_HEIGHT && diffY <= 1.2491870787) {
          List<Packet> sendList = Lists.newArrayList();
          // if this is true, this must be a step
          // now to send additional packets to get around NCP
          double x = previousPositionPacket.getX(0.D);
          double y = previousPositionPacket.getY(0.D);
          double z = previousPositionPacket.getZ(0.D);
          sendList.add(new CPacketPlayer.Position(x, y + 0.4199999869D, z, true));
          sendList.add(new CPacketPlayer.Position(x, y + 0.7531999805D, z, true));
          sendList.add(
            new CPacketPlayer.Position(
              packetPlayer.getX(0.f),
              packetPlayer.getY(0.f),
              packetPlayer.getZ(0.f),
              packetPlayer.isOnGround()));
          for (Packet toSend : sendList) {
            PacketHelper.ignore(toSend);
            getNetworkManager().sendPacket(toSend);
          }
          event.setCanceled(true);
        }
      }
      previousPositionPacket = (CPacketPlayer) event.getPacket();
    }
  }
  
  @Override
  public String getDebugDisplayText() {
    return String.format(
      "%s[%s%s]",
      super.getDisplayText(),
      stepHeight.get().toString(),
      unstep.get() ? "+unstep" : ""
    );
  }
}
