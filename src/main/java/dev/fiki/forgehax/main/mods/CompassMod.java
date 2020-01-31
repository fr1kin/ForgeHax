package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.events.Render2DEvent;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.draw.SurfaceHelper;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created by Babbaj on 10/28/2017.
 */
@RegisterMod
public class CompassMod extends ToggleMod {
  
  public final Setting<Double> scale =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("scale")
          .description("size of the compass")
          .defaultTo(3.D)
          .build();
  
  private static final double HALF_PI = Math.PI / 2;
  
  private enum Direction {
    N,
    W,
    S,
    E
  }
  
  public CompassMod() {
    super(Category.RENDER, "Compass", false, "cool compass overlay");
  }
  
  @SubscribeEvent
  public void onRender(Render2DEvent event) {
    final double centerX = event.getScreenWidth() / 2.D;
    final double centerY = event.getScreenHeight() * 0.8D;
    
    for (Direction dir : Direction.values()) {
      double rad = getPosOnCompass(dir);
      SurfaceHelper.drawTextShadowCentered(
          dir.name(),
          (float) (centerX + getX(rad)),
          (float) (centerY + getY(rad)),
          dir == Direction.N ? Colors.RED.toBuffer() : Colors.WHITE.toBuffer());
      
    }
    
  }
  
  private double getX(double rad) {
    return Math.sin(rad) * (scale.getAsDouble() * 10);
  }
  
  private double getY(double rad) {
    final double epicPitch = MathHelper
        .clamp(Globals.getLocalPlayer().rotationPitch + 30f, -90f, 90f);
    final double pitchRadians = Math.toRadians(epicPitch); // player pitch
    return Math.cos(rad) * Math.sin(pitchRadians) * (scale.getAsDouble() * 10);
  }
  
  // return the position on the circle in radians
  private static double getPosOnCompass(Direction dir) {
    double yaw =
        Math.toRadians(
            MathHelper.wrapDegrees(Globals.getLocalPlayer().rotationYaw)); // player yaw
    int index = dir.ordinal();
    return yaw + (index * HALF_PI);
  }
}
