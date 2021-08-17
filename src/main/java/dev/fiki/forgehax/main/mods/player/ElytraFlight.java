package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.Switch.Handle;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.EnumSetting;
import dev.fiki.forgehax.api.cmd.settings.FloatSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.movement.ClampMotionSpeedEvent;
import dev.fiki.forgehax.asm.events.movement.ElytraFlyMovementEvent;
import dev.fiki.forgehax.main.Common;
import net.minecraft.network.play.client.CEntityActionPacket;

import static dev.fiki.forgehax.main.Common.getGameSettings;
import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "ElytraFlight",
    description = "Elytra flight",
    category = Category.PLAYER
)
public class ElytraFlight extends ToggleMod {
  enum FlyMode {
    FLIGHT,
    SLOW_FALL,
    ;
  }

  public final BooleanSetting flyOnEnable = newBooleanSetting()
      .name("fly-on-enable")
      .description("Start flying when enabled")
      .defaultTo(false)
      .build();

  public final FloatSetting speed = newFloatSetting()
      .name("speed")
      .description("Movement speed")
      .defaultTo(0.05f)
      .build();

  private final EnumSetting<FlyMode> mode = newEnumSetting(FlyMode.class)
      .name("mode")
      .description("Elytra flight mode")
      .defaultTo(FlyMode.SLOW_FALL)
      .build();

  private final Handle flying = LocalPlayerEx.getFlySwitch().createHandle(getName());

  @Override
  protected void onEnabled() {
    if (flyOnEnable.getValue()) {
      Common.addScheduledTask(() -> {
        if (getLocalPlayer() != null && !getLocalPlayer().isFallFlying()) {
          Common.sendNetworkPacket(new CEntityActionPacket(getLocalPlayer(), CEntityActionPacket.Action.START_FALL_FLYING));
        }
      });
    }
  }

  @Override
  public void onDisabled() {
    flying.disable();

    // Are we still here?
    if (FlyMode.FLIGHT.equals(mode.getValue()) && getLocalPlayer() != null) {
      // Ensure the player starts flying again.
      Common.sendNetworkPacket(new CEntityActionPacket(getLocalPlayer(), CEntityActionPacket.Action.START_FALL_FLYING));
    }
  }

  @SubscribeListener
  public void onElytraMovement(ElytraFlyMovementEvent event) {
    if(!FlyMode.FLIGHT.equals(mode.getValue())) {
      event.setCanceled(true);
    }
  }

  @SubscribeListener
  public void onClampMotion(ClampMotionSpeedEvent event) {
    if(!FlyMode.FLIGHT.equals(mode.getValue())) {
      event.setCanceled(true);
    }
  }

  @SubscribeListener
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if(FlyMode.FLIGHT.equals(mode.getValue())) {
      if (getLocalPlayer().isFallFlying()) {
        flying.enable();
      }
      getLocalPlayer().abilities.setFlyingSpeed(speed.getValue());
    } else {
      if (!getLocalPlayer().isFallFlying()) {
        return;
      }

      double motionX = 0.0D;
      double motionY = -0.0001D;
      double motionZ = 0.0D;

      final float speed = (float) (1.7F * 1.06);

      double forward = getLocalPlayer().input.forwardImpulse;
      double strafe = getLocalPlayer().input.leftImpulse;
      float yaw = getLocalPlayer().yRot;

      if ((forward == 0.0D) && (strafe == 0.0D)) {
        motionX = 0.0D;
        motionZ = 0.0D;
      } else {
        if (forward != 0.0D) {
          if (strafe > 0.0D) {
            yaw += (forward > 0.0D ? -45 : 45);
          } else if (strafe < 0.0D) {
            yaw += (forward > 0.0D ? 45 : -45);
          }

          strafe = 0.0D;
          if (forward > 0.0D) {
            forward = 1.0D;
          } else if (forward < 0.0D) {
            forward = -1.0D;
          }
        }
        final double cos = Math.cos(Math.toRadians(yaw + 90.0F));
        final double sin = Math.sin(Math.toRadians(yaw + 90.0F));
        motionX = (forward * speed * cos + strafe * speed * sin);
        motionZ = (forward * speed * sin - strafe * speed * cos);

      }
      if (getGameSettings().keyShift.isDown()) {
        motionY = -1.0D;
      }

      getLocalPlayer().setDeltaMovement(motionX, motionY, motionZ);
    }
  }
}
