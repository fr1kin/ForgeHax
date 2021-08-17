package dev.fiki.forgehax.main.services;

import dev.fiki.forgehax.api.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.api.common.PriorityEnum;
import dev.fiki.forgehax.api.event.EventListener;
import dev.fiki.forgehax.api.event.ListenerFlags;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.PlayerRotationEvent;
import dev.fiki.forgehax.api.events.world.WorldUnloadEvent;
import dev.fiki.forgehax.api.extension.GeneralEx;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.math.Angle;
import dev.fiki.forgehax.api.math.AngleUtil;
import dev.fiki.forgehax.api.mod.ServiceMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.movement.PostMovementUpdateEvent;
import dev.fiki.forgehax.asm.events.movement.PreMovementUpdateEvent;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

/**
 * Created on 6/15/2017 by fr1kin
 */
@RegisterMod
@ExtensionMethod({GeneralEx.class, LocalPlayerEx.class})
public class RotationService extends ServiceMod {
  public final DoubleSetting smooth = newDoubleSetting()
      .name("smooth")
      .description("Angle smoothing for bypassing anti-cheats. Set to 0 to disable")
      .defaultTo(45.D)
      .min(0.D)
      .max(180.D)
      .build();

  @Getter
  private Angle previousViewAngles = null;
  private Runnable rotationTask = null;
  private boolean silent = true;

  @SubscribeListener
  public void onWorldUnload(WorldUnloadEvent event) {
    LocalPlayerEx.setServerAngles(null);
    previousViewAngles = null;
    rotationTask = null;
    silent = false;
  }

  @SubscribeListener(priority = PriorityEnum.HIGHEST,
      flags = ListenerFlags.ALLOW_CANCELED | ListenerFlags.ALLOW_IGNORED_PACKETS)
  public void onMovementUpdatePre(PreMovementUpdateEvent event) {
    final Angle clientAngles = event.getLocalPlayer().getViewAngles();
    final Angle serverAngles = LocalPlayerEx.getServerAngles() == null ? clientAngles : LocalPlayerEx.getServerAngles();

    Angle targetAngles = null;
    Runnable task = null;
    boolean silentChange = true;

    for (EventListener listener : PlayerRotationEvent.listenerList()) {
      // create a new event each time so that previous events do not interfere with others
      PlayerRotationEvent rotationEvent = new PlayerRotationEvent(event.getLocalPlayer().getViewAngles(), serverAngles);

      listener.run(rotationEvent);

      // the first event that changes the players viewing angles will be given focus
      if (!rotationEvent.isCanceled() && rotationEvent.isUpdated()) {
        targetAngles = rotationEvent.getViewAngles();
        task = rotationEvent.getFocusTask();
        silentChange = rotationEvent.isSilent();
        break;
      }
    }

    // no changes made, so we dont do anything
    if (targetAngles == null) {
      return;
    }

    previousViewAngles = event.getLocalPlayer().getViewAngles();
    // should we not change the angles the client sees?
    silent = silentChange;

    if (smooth.floatValue() > 0.f) {
      // serverAngles = the direction we are looking at on the server side
      // targetAngles = the direction we want to start looking at
      Angle aim = clampAngle(serverAngles, targetAngles, smooth.floatValue()).inDegrees().normalize();
      event.getLocalPlayer().setViewAngles(aim);

      // only execute task if we are in range of it
      if (getRotationCount(serverAngles, targetAngles, smooth.floatValue()) <= 1.f) {
        rotationTask = task;
      }
    }
  }

  @SubscribeListener(priority = PriorityEnum.HIGHEST,
      flags = ListenerFlags.ALLOW_CANCELED | ListenerFlags.ALLOW_IGNORED_PACKETS)
  public void onMovementUpdatePost(PostMovementUpdateEvent event) {
    LocalPlayerEx.setServerAngles(event.getLocalPlayer().getViewAngles());

    Angle prev = previousViewAngles;
    if (prev != null) {
      // discard reference
      previousViewAngles = null;

      // revert to exact previous view angles (if desired)
      if (silent) {
        event.getLocalPlayer().setViewAnglesRaw(prev.getPitch(), prev.getYaw());
      }

      // run task if one exists
      Runnable task = rotationTask;
      if (task != null) {
        task.run();
        // discard reference
        rotationTask = null;
      }
    }
  }

  @SubscribeListener(priority = PriorityEnum.HIGHEST,
      flags = ListenerFlags.ALLOW_CANCELED | ListenerFlags.ALLOW_IGNORED_PACKETS)
  public void onPacketReceived(PacketInboundEvent event) {
    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      // when the server sets the rotation we use that instead
      final SPlayerPositionLookPacket packet = (SPlayerPositionLookPacket) event.getPacket();

      float pitch = packet.getXRot();
      float yaw = packet.getYRot();

      Angle va = getLocalPlayer().getViewAngles();

      if (packet.getRelativeArguments().contains(SPlayerPositionLookPacket.Flags.X_ROT)) {
        pitch += va.getPitch();
      }

      if (packet.getRelativeArguments().contains(SPlayerPositionLookPacket.Flags.Y_ROT)) {
        yaw += va.getYaw();
      }

      LocalPlayerEx.setServerAngles(Angle.degrees(pitch, yaw));
    }
  }

  private static float clampAngle(float from, float to, float clamp) {
    return AngleUtil.normalizeInDegrees(
        from + GeneralEx.clamp(AngleUtil.normalizeInDegrees(to - from), -clamp, clamp));
  }

  private static Angle clampAngle(Angle from, Angle to, float clamp) {
    return Angle.degrees(
        clampAngle(from.getPitch(), to.getPitch(), clamp),
        clampAngle(from.getYaw(), to.getYaw(), clamp));
  }

  private static float getRotationCount(Angle from, Angle to, float clamp) {
    Angle diff = to.sub(from).normalize();
    float rp = (diff.getPitch() / clamp);
    float ry = (diff.getYaw() / clamp);
    return Math.max(Math.abs(rp), Math.abs(ry));
  }
}
