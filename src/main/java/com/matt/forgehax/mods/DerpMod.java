package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getModManager;
import static com.matt.forgehax.util.PacketHelper.ignoreAndSend;
import static com.matt.forgehax.util.PacketHelper.isIgnored;
import static com.matt.forgehax.util.PacketHelper.send;

/**
 * Coded by LoganDark on 8/21/2019
 */
@RegisterMod
public class DerpMod extends ToggleMod {
  public DerpMod() {
    super(Category.PLAYER, "Derp", false, "Derp");
  }

  private float error;
  private boolean sneaking;
  private TimerMod timerMod;

  public final Setting<Float> speed =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("speed")
          .description("Approximate derps per tick")
          .defaultTo(1f)
          .changed(__ -> error = 0)
          .build();

  public final Setting<Boolean> timerSync =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("timer-sync")
          .description("Preserve original speed even with Timer enabled")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> rotate =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("rotate")
          .description("Randomly rotate head every derp")
          .changed(change -> MC.addScheduledTask(() -> {
            if (isEnabled() && !change.getTo()) rotateDisabled();
          }))
          .defaultTo(true)
          .build();

  public final Setting<Float> rotateChance =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("rotate-chance")
          .description("Chance to rotate for every derp")
          .min(0f)
          .max(1f)
          .defaultTo(1f)
          .build();

  public final Setting<Boolean> sneak =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("sneak")
          .description("Toggle sneak every derp")
          .changed(change -> MC.addScheduledTask(() -> {
            if (!isEnabled()) return;
            if (!change.getTo()) sneakDisabled();
            else {
              EntityPlayer player = getLocalPlayer();
              sneaking = player != null && player.isSneaking();
            }
          }))
          .defaultTo(false)
          .build();

  public final Setting<Float> sneakChance =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("sneak-chance")
          .description("Chance to sneak for every derp")
          .min(0f)
          .max(1f)
          .defaultTo(1f)
          .build();

  public final Setting<Boolean> hit =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("hit")
          .description("Randomly hit every derp")
          .defaultTo(true)
          .build();

  public final Setting<Float> hitChance =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("hit-chance")
          .description("Chance to hit for every derp")
          .min(0f)
          .max(1f)
          .defaultTo(.25f)
          .build();

  public final Setting<Float> hitPan =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("hit-pan")
          .description("Chance for any hit to be the off hand")
          .min(0f)
          .max(1f)
          .defaultTo(.5f)
          .build();

  private void sendRotatePacket(EntityPlayer player, float pitch, float yaw) {
    ignoreAndSend(new CPacketPlayer.Rotation(yaw, pitch, player.onGround));
  }

  private void rotateDisabled() {
    EntityPlayer player = getLocalPlayer();
    if (player == null) return;

    sendRotatePacket(player, player.rotationPitch, player.rotationYawHead);
  }

  private CPacketEntityAction.Action actionFromSneaking(boolean sneaking) {
    return sneaking
        ? CPacketEntityAction.Action.START_SNEAKING
        : CPacketEntityAction.Action.STOP_SNEAKING;
  }

  private void sendSneakPacket(EntityPlayer player, boolean sneaking) {
    this.sneaking = sneaking;
    ignoreAndSend(new CPacketEntityAction(player, actionFromSneaking(sneaking)));
  }

  private void sneakDisabled() {
    EntityPlayer player = getLocalPlayer();
    if (player == null) return;

    sendSneakPacket(player, player.isSneaking());
  }

  private void sendHitPacket(EnumHand hand) {
    ignoreAndSend(new CPacketAnimation(hand));
  }

  private boolean chance(float chance) {
    return chance == 1f || (chance != 0f && Math.random() <= chance);
  }

  @Override
  protected void onEnabled() {
    super.onEnabled();

    error = 0;

    EntityPlayer player = getLocalPlayer();
    sneaking = player != null && player.isSneaking();

    timerMod = getModManager().get(TimerMod.class).orElse(null);
  }

  @Override
  protected void onDisabled() {
    if (rotate.get()) rotateDisabled();
    if (sneak.get()) sneakDisabled();
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    EntityPlayer player = (EntityPlayer) event.getEntityLiving();
    if (player == null) return;
    Random rng = player.getRNG();

    float effectiveSpeed = speed.get();

    if (timerSync.get() && timerMod != null && timerMod.isEnabled())
      effectiveSpeed /= timerMod.factor.get();

    error += effectiveSpeed;
    int iter = (int) error;
    error -= iter;

    for (int i = 0; i < iter; i++) {
      if (rotate.get() && chance(rotateChance.get()))
        sendRotatePacket(player, rng.nextInt(180) - 90, rng.nextInt(360) - 180);

      if (sneak.get() && chance(sneakChance.get()))
        sendSneakPacket(player, !sneaking);

      if (hit.get() && chance(hitChance.get()))
        sendHitPacket(chance(hitPan.get()) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
    }
  }

  @SubscribeEvent
  public void onPacketSending(PacketEvent.Outgoing.Pre event) {
    Packet<?> packet = event.getPacket();
    if (isIgnored(packet)) return;

    if (rotate.get() && (packet instanceof CPacketPlayer.Rotation || packet instanceof CPacketPlayer.PositionRotation)) {
      event.setCanceled(true);

      if (packet instanceof CPacketPlayer.PositionRotation) {
        send(new CPacketPlayer.Position(
            ((CPacketPlayer) packet).getX(0),
            ((CPacketPlayer) packet).getY(0),
            ((CPacketPlayer) packet).getZ(0),
            ((CPacketPlayer) packet).isOnGround()
        ));
      }
    } else if (sneak.get() && packet instanceof CPacketEntityAction) {
      CPacketEntityAction.Action action = ((CPacketEntityAction) packet).getAction();

      if (action == CPacketEntityAction.Action.START_SNEAKING || action == CPacketEntityAction.Action.STOP_SNEAKING)
        sneaking = action == CPacketEntityAction.Action.START_SNEAKING;
    } else if (hit.get() && packet instanceof CPacketAnimation)
      event.setCanceled(true);
  }
}
