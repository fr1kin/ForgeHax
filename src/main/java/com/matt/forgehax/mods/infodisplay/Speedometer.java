package com.matt.forgehax.mods.infodisplay;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.math.MathHelper;

@RegisterMod
public class Speedometer extends ToggleMod {

  public Speedometer() {
    super(Category.GUI, "Speedometer", true, "Shows speed o' meter");
  }

  public final Setting<SpeedUnitTypes> speedUnit =
      getCommandStub()
          .builders()
          .<SpeedUnitTypes>newSettingEnumBuilder()
          .name("unit")
          .description("Choose between KM/H or M/S")
          .defaultTo(SpeedUnitTypes.KMH)
          .build();

  @Override
  public boolean isInfoDisplayElement() {
    return true;
  }

  private double calculateTimerSpeed() {
    // Gets current tps via FastReflection timer
    final float currentTps = FastReflection.Fields.Timer_tickLength.get(FastReflection.Fields.Minecraft_timer.get(MC)) / 1000.0f;

    final double diffPosX = MC.player.posX - MC.player.prevPosX;
    final double diffPosZ = MC.player.posZ - MC.player.prevPosZ;

    final double sqrtVal = Math.pow(diffPosX, 2) + Math.pow(diffPosZ, 2);
    return ((MathHelper.sqrt(sqrtVal) / currentTps) * speedUnit.get().getMultiplier());
  }

  public String getInfoDisplayText() {
    return "Speed: " + calculateTimerSpeed() + " " + speedUnit.get().getString();
  }

  public enum SpeedUnitTypes {
    KMH("km/h", 3.6D),
    MS("m/s", 1D);

    private final String string;
    private final double multiplier;

    SpeedUnitTypes(final String string, final double multiplier) {
      this.string = string;
      this.multiplier = multiplier;
    }

    public String getString() {
      return string;
    }

    public double getMultiplier() {
      return multiplier;
    }
  }
}
