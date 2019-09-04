package com.matt.forgehax.util.command.options;

import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.color.Color;
import com.matt.forgehax.util.command.ExecuteData;
import net.minecraft.util.math.MathHelper;

/**
 * Created on 6/6/2017 by fr1kin
 */
public class OptionProcessors {
  
  public static void rgba(ExecuteData data) {
    int r = SafeConverter.toInteger(data.getOption("red"), 255);
    int g = SafeConverter.toInteger(data.getOption("green"), 255);
    int b = SafeConverter.toInteger(data.getOption("blue"), 255);
    int a = SafeConverter.toInteger(data.getOption("alpha"), 255);
    data.set(
      "colorBuffer",
      Color.of(
        MathHelper.clamp(r, 0, 255),
        MathHelper.clamp(g, 0, 255),
        MathHelper.clamp(b, 0, 255),
        MathHelper.clamp(a, 0, 255)
      ).toBuffer());
    data.set(
      "isColorPresent",
      data.hasOption("red")
        || data.hasOption("green")
        || data.hasOption("blue")
        || data.hasOption("alpha"));
  }
  
  public static void meta(ExecuteData data) {
    data.set("meta", SafeConverter.toInteger(data.getOption("meta"), 0));
  }
}
