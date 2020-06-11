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
    //tickQueue = new LinkedList<>();
  }

  /*private final Setting<Integer> showSpeedPerTicks =
    getCommandStub()
      .builders()
      .<Integer>newSettingBuilder()
      .name("speed-per-ticks")
      .description("Shows the speed you had between these ticks")
      .defaultTo(20)
      .build();*/

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

  public final Setting<SpeedUnitTypes> speedUnit =
    getCommandStub()
      .builders()
      .<SpeedUnitTypes>newSettingEnumBuilder()
      .name("unit")
      .description("Choose between KM/H or M/S")
      .defaultTo(SpeedUnitTypes.KMH)
      .build();

  public final Setting<Integer> roundto =
    getCommandStub()
      .builders()
      .<Integer>newSettingBuilder()
      .name("roundto")
      .description("How many digits after the comma")
      .defaultTo(1)
      .min(0)
      .build();

  //private final LinkedList<SpeedPerTickElement> tickQueue;

  @Override
  public boolean isInfoDisplayElement() {
    return true;
  }

  @Override
  public boolean notInList() {
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

  /*private double calculateSpeedPerTicks(final int ticks) {
    final SpeedPerTickElement newElement = new SpeedPerTickElement(MC.player.posX, MC.player.posZ);
    tickQueue.add(newElement);
    final double speed = newElement.calculateSpeed(tickQueue.getFirst(), speedUnit.get().getMultiplier(), roundto);

    while (tickQueue.size() > ticks) { // a while so we dont stack up a lot of elements in the queue (shouldnt happen.. but who knows)
      tickQueue.removeFirst();
    }
    return speed;
  }*/

  public String getInfoDisplayText() {
    //if (!showSpeedPerTicks.getAsBoolean()) {
	String format = ("%." + roundto.get() + "f");
    return "Speed: " + String.format(format, calculateTimerSpeed()) + " " + speedUnit.get().getString();
    //} else return "Speed: " + calculateSpeedPerTicks(final int ticks) + " " + speedUnit.get().getString();
  }

  private static class SpeedPerTickElement {
    private final long time;
    private final double x;
    private final double z;

    public SpeedPerTickElement(final double x, final double z) {
      this.time = System.currentTimeMillis();
      this.x = x;
      this.z = z;
    }

    /**
     * we request an element that got generated earlier so we dont care if the speed is negative... might have some use case?
     * @param earlier the earlier element.
     * @param multiplier constant for the speed unit.
     * @param roundto how many digits after the comma.
     * @return returns the speed.
     */
    public double calculateSpeed(final SpeedPerTickElement earlier, final double multiplier, final int roundto) {
      final double deltaX = this.x - earlier.x;
      final double deltaZ = this.z - earlier.z;
      final long deltaTime = this.time - earlier.time;
      if(deltaTime == 0) {
        return 0;
      }
      return Math.round((MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ) * multiplier * 1000D / deltaTime) * 10)/10;
    }
  }
}
