package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.FloatSetting;
import dev.fiki.forgehax.api.common.PriorityEnum;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.math.Angle;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.main.managers.RotationManager;
import dev.fiki.forgehax.main.managers.RotationManager.RotationState;
import lombok.experimental.ExtensionMethod;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "YawLock",
    description = "Locks yaw to prevent moving into walls",
    category = Category.PLAYER
)
@ExtensionMethod({LocalPlayerEx.class})
public class YawLockMod extends ToggleMod
    implements RotationManager.MovementUpdateListener {

  public final BooleanSetting auto = newBooleanSetting()
      .name("auto")
      .description("Automatically finds angle to snap to based on the direction you're facing")
      .defaultTo(true)
      .build();

  public final FloatSetting angle = newFloatSetting()
      .name("angle")
      .description("Angle to snap too")
      .defaultTo(0.f)
      .min(-180.f)
      .max(180.f)
      .build();

  private float getYawDirection(float yaw) {
    return Math.round(Math.round((yaw + 1.f) / 45.f) * 45.f);
  }

  private Angle getSnapAngle() {
    Angle va = getLocalPlayer().getViewAngles().normalize();
    return va.setYaw(auto.getValue() ? getYawDirection(va.getYaw()) : angle.getValue());
  }

  @Override
  public String getDebugDisplayText() {
    return super.getDebugDisplayText() + " [" + String.format("%.4f", getSnapAngle().getYaw()) + "]";
  }

  @Override
  protected void onEnabled() {
    RotationManager.getManager().register(this, PriorityEnum.LOWEST);
  }

  @Override
  protected void onDisabled() {
    RotationManager.getManager().unregister(this);
  }

  @Override
  public void onLocalPlayerMovementUpdate(RotationState.Local state) {
    state.setViewAngles(getSnapAngle());
  }
}
