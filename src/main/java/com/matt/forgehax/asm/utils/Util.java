package com.matt.forgehax.asm.utils;

import net.minecraft.launchwrapper.Launch;

public class Util {

  public static boolean isOptifinePresent() {
    try {
      Class.forName("Config", false, Launch.classLoader);
      return true;
    } catch (Throwable t) {
      return false;
    }
  }

}
