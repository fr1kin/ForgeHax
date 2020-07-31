package dev.fiki.forgehax.asm.utils.asmtype;

import org.objectweb.asm.Type;

import java.util.Objects;

class Util {
  public static String emptyToNull(String o) {
    return (o == null || o.isEmpty()) ? null : o;
  }

  public static <T> T firstNonNull(T o1, T o2) {
    return o1 != null ? o1 : Objects.requireNonNull(o2);
  }

  public static <T> T firstNonNull(T o1, T o2, T o3) {
    return o1 != null ? o1 : (o2 != null ? o2 : Objects.requireNonNull(o3));
  }

  public static Type descriptorToTypeOrNull(String desc) {
    return emptyToNull(desc) == null ? null : Type.getMethodType(desc);
  }
}
