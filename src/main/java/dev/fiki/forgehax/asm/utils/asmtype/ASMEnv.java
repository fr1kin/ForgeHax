package dev.fiki.forgehax.asm.utils.asmtype;

import dev.fiki.forgehax.api.asm.runtime.Format;

public class ASMEnv {
  static volatile int current = 0;

  public static void setCurrentClassFormat(Format format) {
    current = format.ordinal();
  }

  public static Format getCurrentClassFormat() {
    return Format.values()[current];
  }
}
