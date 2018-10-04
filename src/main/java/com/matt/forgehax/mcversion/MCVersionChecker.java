package com.matt.forgehax.mcversion;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import net.minecraftforge.common.ForgeVersion;

/** Created on 5/29/2017 by fr1kin */
public class MCVersionChecker {
  public static String getMcVersion() {
    return ForgeVersion.mcVersion;
  }

  private static boolean checkVersion(MCVersions mcVersions) {
    if (mcVersions != null) {
      for (String version : mcVersions.value())
        if (Objects.equals(getMcVersion(), version)) return true;
      return false;
    } else
      return true; // if it isn't marked with MCVersions just assume its compatible with the current
    // version
  }

  public static boolean checkVersion(AnnotatedElement element) {
    return checkVersion(element.getAnnotation(MCVersions.class));
  }

  public static void requireValidVersion(AnnotatedElement element)
      throws IncompatibleMCVersionException {
    if (!checkVersion(element))
      throw new IncompatibleMCVersionException(
          String.format("Incompatible with current Minecraft version %s", getMcVersion()));
  }
}
