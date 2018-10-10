package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.mods.managers.PositionRotationManager.RotationState;
import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.math.AngleHelper;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

@RegisterMod
public class YawLockMod extends ToggleMod
    implements PositionRotationManager.MovementUpdateListener {
  public final Setting<Boolean> auto =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("auto")
          .description("Automatically finds angle to snap to based on the direction you're facing")
          .defaultTo(true)
          .build();

  public final Setting<Double> angle =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("angle")
          .description("Angle to snap too")
          .defaultTo(0.0D)
          .min(-180D)
          .max(180D)
          .build();

  public YawLockMod() {
    super(Category.PLAYER, "YawLock", false, "Locks yaw to prevent moving into walls");
  }

  private double getYawDirection() {
    return (int) (Math.round((LocalPlayerUtils.getViewAngles().getYaw() + 1.f) / 45.f) * 45.f);
  }

  private Angle getSnapAngle() {
    return Angle.degrees(
        LocalPlayerUtils.getViewAngles().getPitch(), auto.get() ? getYawDirection() : angle.get());
  }

  @Override
  protected void onLoad() {
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("snap")
        .description("Snap once to a certain direction")
        .processor(
            data -> {
              if (getLocalPlayer() == null || getWorld() == null) return;

              final double angle =
                  data.getArgumentCount() == 0
                      ? getYawDirection()
                      : AngleHelper.normalizeInDegrees(
                          SafeConverter.toDouble(data.getArgumentAsString(0)));

              PositionRotationManager.getManager()
                  .registerTemporary(
                      state ->
                          state.setViewAngles(
                              Angle.degrees(state.getClientAngles().getPitch(), angle)));
            });
  }

  @Override
  protected void onEnabled() {
    PositionRotationManager.getManager().register(this);
  }

  @Override
  protected void onDisabled() {
    PositionRotationManager.getManager().unregister(this);
  }

  @Override
  public void onLocalPlayerMovementUpdate(RotationState.Local state) {
    state.setClientAngles(getSnapAngle());
  }
}
