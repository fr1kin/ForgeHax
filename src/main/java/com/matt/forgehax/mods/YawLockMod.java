package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.mods.managers.PositionRotationManager.RotationState;
import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.common.PriorityEnum;
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
  
  public final Setting<Float> angle =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("angle")
          .description("Angle to snap too")
          .defaultTo(0.f)
          .min(-180.f)
          .max(180.f)
          .build();
  
  public YawLockMod() {
    super(Category.MOVEMENT, "YawLock", false, "Locks yaw to prevent moving into walls");
  }
  
  private float getYawDirection(float yaw) {
    return Math.round(Math.round((yaw + 1.f) / 45.f) * 45.f);
  }
  
  private Angle getSnapAngle() {
    Angle va = LocalPlayerUtils.getViewAngles().normalize();
    return va.setYaw(auto.get() ? getYawDirection(va.getYaw()) : angle.get());
  }
  
  @Override
  public String getDebugDisplayText() {
    return super.getDebugDisplayText() + " [" +  String.format("%.4f", getSnapAngle().getYaw()) + "]";
  }
  
  @Override
  protected void onLoad() {
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("snap")
        .description("Snap once to a certain direction")
        .processor(data -> MC.addScheduledTask(() -> {
            if (getLocalPlayer() == null || getWorld() == null) {
              return;
            }
            
            final float angle = data.getArgumentCount() == 0 ? getSnapAngle().getPitch()
                : AngleHelper.normalizeInDegrees(
                    Float.parseFloat(data.getArgumentAsString(0)));
            
            PositionRotationManager.getManager().registerTemporary(state
                -> state.setViewAngles(Angle.degrees(state.getClientAngles().getPitch(), angle)));
        }))
        .build();
  }
  
  @Override
  protected void onEnabled() {
    PositionRotationManager.getManager().register(this, PriorityEnum.LOWEST);
  }
  
  @Override
  protected void onDisabled() {
    PositionRotationManager.getManager().unregister(this);
  }
  
  @Override
  public void onLocalPlayerMovementUpdate(RotationState.Local state) {
    state.setViewAngles(getSnapAngle());
  }
}
