package com.matt.forgehax.mods;

import com.matt.forgehax.Helper;
import com.matt.forgehax.events.Render2DEvent;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.ArrayUtils;

/** Created by Babbaj on 10/28/2017. */
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

  private final double HALF_PI = Math.PI / 2;

  private final String[] DIRECTIONS = {"N", "W", "S", "E"};

  public CompassMod() {
    super(Category.RENDER, "Compass", false, "cool compass overlay");
  }

  @SubscribeEvent
  public void onRender(Render2DEvent event) {
    final double centerX = event.getScreenWidth() / 2;
    final double centerY = event.getScreenHeight() * 0.8;

    for (String dir : DIRECTIONS) {
      double rad = getPosOnCompass(dir);
      SurfaceHelper.drawTextShadowCentered(
          dir,
          (float) (centerX + getX(rad)),
          (float) (centerY + getY(rad)),
          dir.equals("N") ? Utils.Colors.RED : Utils.Colors.WHITE);
    }
  }

  private double getX(double rad) {
    return Math.sin(rad) * (scale.getAsDouble() * 10);
  }

  private double getY(double rad) {
    double pitch = Math.toRadians(Helper.getLocalPlayer().rotationPitch); // player pitch
    return Math.cos(rad) * Math.sin(pitch) * (scale.getAsDouble() * 10);
  }

  // return the position on the circle in radians
  private double getPosOnCompass(String s) {
    double yaw =
        Math.toRadians(
            MathHelper.wrapDegrees(Helper.getLocalPlayer().rotationYaw - 90)); // player yaw
    int index = ArrayUtils.indexOf(DIRECTIONS, s) + 1;
    return yaw + (index * HALF_PI);
  }
}
