package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.movement.ApplyCollisionMotionEvent;
import dev.fiki.forgehax.common.events.movement.EntityBlockSlipApplyEvent;
import dev.fiki.forgehax.common.events.movement.PushedByLiquidEvent;
import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.math.VectorUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ConcurrentModificationException;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod
public class AntiKnockbackMod extends ToggleMod {

  private final DoubleSetting multiplier_x = newDoubleSetting()
      .name("x-multiplier")
      .description("Multiplier for X axis")
      .defaultTo(0.D)
      .build();

  private final DoubleSetting multiplier_y = newDoubleSetting()
      .name("y-multiplier")
      .description("Multiplier for Y axis")
      .defaultTo(0.D)
      .build();

  private final DoubleSetting multiplier_z = newDoubleSetting()
      .name("z-multiplier")
      .description("Multiplier for Z axis")
      .defaultTo(0.D)
      .build();

  private final BooleanSetting explosions = newBooleanSetting()
      .name("explosions")
      .description("Disable velocity from SPacketExplosion")
      .defaultTo(true)
      .build();

  private final BooleanSetting velocity = newBooleanSetting()
      .name("velocity")
      .description("Disable velocity from SPacketEntityVelocity")
      .defaultTo(true)
      .build();

  private final BooleanSetting fishhook = newBooleanSetting()
      .name("fishhook")
      .description("Disable velocity from a fishhook")
      .defaultTo(true)
      .build();

  private final BooleanSetting water = newBooleanSetting()
      .name("water")
      .description("Disable velocity from flowing water")
      .defaultTo(true)
      .build();

  private final BooleanSetting push = newBooleanSetting()
      .name("push")
      .description("Disable velocity from entity pushing")
      .defaultTo(true)
      .build();

  private final BooleanSetting blocks = newBooleanSetting()
      .name("blocks")
      .description("Disable velocity from block pushing")
      .defaultTo(true)
      .build();

  private final BooleanSetting slipping = newBooleanSetting()
      .name("slipping")
      .description("Disable velocity from ice slipping")
      .defaultTo(true)
      .build();

  public AntiKnockbackMod() {
    super(Category.COMBAT, "AntiKnockback", false, "Removes knockback movement");
  }

  private Vector3d getMultiplier() {
    return new Vector3d(multiplier_x.getValue(), multiplier_y.getValue(), multiplier_z.getValue());
  }

  private Vector3d getPacketMotion(IPacket<?> packet) {
    if (packet instanceof SExplosionPacket) {
      return new Vector3d(
          FastReflection.Fields.SExplosionPacket_motionX.get(packet),
          FastReflection.Fields.SExplosionPacket_motionY.get(packet),
          FastReflection.Fields.SExplosionPacket_motionZ.get(packet));
    } else if (packet instanceof SEntityVelocityPacket) {
      return new Vector3d(
          FastReflection.Fields.SEntityVelocityPacket_motionX.get(packet),
          FastReflection.Fields.SEntityVelocityPacket_motionY.get(packet),
          FastReflection.Fields.SEntityVelocityPacket_motionZ.get(packet));
    } else {
      throw new IllegalArgumentException();
    }
  }

  private void setPacketMotion(IPacket<?> packet, Vector3d in) {
    if (packet instanceof SExplosionPacket) {
      FastReflection.Fields.SExplosionPacket_motionX.set(packet, (float) in.x);
      FastReflection.Fields.SExplosionPacket_motionY.set(packet, (float) in.y);
      FastReflection.Fields.SExplosionPacket_motionZ.set(packet, (float) in.z);
    } else if (packet instanceof SEntityVelocityPacket) {
      FastReflection.Fields.SEntityVelocityPacket_motionX.set(packet, (int) Math.round(in.x));
      FastReflection.Fields.SEntityVelocityPacket_motionY.set(packet, (int) Math.round(in.y));
      FastReflection.Fields.SEntityVelocityPacket_motionZ.set(packet, (int) Math.round(in.z));
    } else {
      throw new IllegalArgumentException();
    }
  }

  private void addEntityVelocity(Entity in, Vector3d velocity) {
    in.setMotion(in.getMotion().add(velocity));
  }

  /**
   * Stops TNT and knockback velocity
   */
  @SubscribeEvent
  public void onPacketReceived(PacketInboundEvent event) {
    if (!isInWorld()) {
      return;
    } else if (explosions.getValue() && event.getPacket() instanceof SExplosionPacket) {
      Vector3d multiplier = getMultiplier();
      Vector3d motion = getPacketMotion(event.getPacket());
      setPacketMotion(event.getPacket(), VectorUtils.multiplyBy(motion, multiplier));
    } else if (velocity.getValue() && event.getPacket() instanceof SEntityVelocityPacket) {
      if (((SEntityVelocityPacket) event.getPacket()).getEntityID() == getLocalPlayer().getEntityId()) {
        Vector3d multiplier = getMultiplier();
        if (multiplier.lengthSquared() > 0.D) {
          setPacketMotion(event.getPacket(),
              VectorUtils.multiplyBy(getPacketMotion(event.getPacket()), multiplier));
        } else {
          event.setCanceled(true);
        }
      }
    } else if (fishhook.getValue() && event.getPacket() instanceof SEntityStatusPacket) {
      // CREDITS TO 0x22
      // fuck you popbob for making me need this
      SEntityStatusPacket packet = (SEntityStatusPacket) event.getPacket();
      if (packet.getOpCode() == 31) {
        try {
          Entity offender = packet.getEntity(getWorld());
          if (offender instanceof FishingBobberEntity) {
            FishingBobberEntity hook = (FishingBobberEntity) offender;
            if (getLocalPlayer().equals(hook.func_234607_k_())) {
              event.setCanceled(true);
            }
          }
        } catch (ConcurrentModificationException e) {
          getLogger().warn("ConcurrentModificationException caused by packet::getEntity");
          event.setCanceled(true);
        }
      }
    }
  }

  /**
   * Stops velocity from collision
   */
  @SubscribeEvent
  public void onApplyCollisionMotion(ApplyCollisionMotionEvent event) {
    if (push.getValue() && getLocalPlayer() != null && getLocalPlayer().equals(event.getEntity())) {
      addEntityVelocity(
          event.getEntity(),
          VectorUtils.multiplyBy(
              new Vector3d(event.getMotionX(), event.getMotionY(), event.getMotionZ()),
              getMultiplier()));
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void onPushOutOfBlocks(PlayerSPPushOutOfBlocksEvent event) {
    if (blocks.getValue()) {
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void onBlockSlip(EntityBlockSlipApplyEvent event) {
    if (slipping.getValue()
        && getLocalPlayer() != null
        && getLocalPlayer().equals(event.getLivingEntity())) {
      event.setSlipperiness(Blocks.STONE.getDefaultState().getSlipperiness(null, null, null));
    }
  }

  @SubscribeEvent
  public void onPushedByLiquid(PushedByLiquidEvent event) {
    if(water.isEnabled()) {
      event.setCanceled(true);
    }
  }
}
