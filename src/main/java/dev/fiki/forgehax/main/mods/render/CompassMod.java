package dev.fiki.forgehax.main.mods.render;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.draw.SurfaceHelper;
import dev.fiki.forgehax.main.util.events.Render2DEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "Compass",
    description = "cool compass overlay",
    category = Category.RENDER
)
public class CompassMod extends ToggleMod {
  public final DoubleSetting scale = newDoubleSetting()
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
    return Math.sin(rad) * (scale.getValue() * 10);
  }

  private double getY(double rad) {
    final double epicPitch = MathHelper
        .clamp(Common.getLocalPlayer().rotationPitch + 30f, -90f, 90f);
    final double pitchRadians = Math.toRadians(epicPitch); // player pitch
    return Math.cos(rad) * Math.sin(pitchRadians) * (scale.getValue() * 10);
  }

  // return the position on the circle in radians
  private static double getPosOnCompass(Direction dir) {
    double yaw =
        Math.toRadians(
            MathHelper.wrapDegrees(Common.getLocalPlayer().rotationYaw)); // player yaw
    int index = dir.ordinal();
    return yaw + (index * HALF_PI);
  }
}
