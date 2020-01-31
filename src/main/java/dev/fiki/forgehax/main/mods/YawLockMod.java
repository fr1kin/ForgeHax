package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.common.PriorityEnum;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.math.Angle;
import dev.fiki.forgehax.main.util.math.AngleHelper;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.mods.managers.PositionRotationManager;
import dev.fiki.forgehax.main.mods.managers.PositionRotationManager.RotationState;

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
    super(Category.PLAYER, "YawLock", false, "Locks yaw to prevent moving into walls");
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
        .processor(data -> Globals.addScheduledTask(() -> {
            if (!Globals.isInWorld()) {
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
