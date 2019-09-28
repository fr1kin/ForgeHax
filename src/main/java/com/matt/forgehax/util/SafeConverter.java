package com.matt.forgehax.util;

import com.matt.forgehax.util.command.CommandHelper;
import java.util.Objects;

/**
 * Created on 5/18/2017 by fr1kin
 */
public class SafeConverter {
  
  private static final String ACCEPTABLE_TRUE_BOOLEAN_STRINGS =
      CommandHelper.join(
          new String[]{Boolean.TRUE.toString(), "t", "on", "enable", "enabled"}, "|");
  
  //
  // BOOLEAN
  //
  public static boolean toBoolean(Object o, boolean defaultValue) {
    try {
      Objects.requireNonNull(o);
      if (o instanceof Boolean) {
        return ((Boolean) o);
      } else {
        String str = String.valueOf(o);
        try {
          return Integer.valueOf(str) != 0;
        } catch (Exception e) {
          return str.toLowerCase().matches(ACCEPTABLE_TRUE_BOOLEAN_STRINGS);
        }
      }
    } catch (Throwable t) {
      return defaultValue;
    }
  }
  
  public static boolean toBoolean(Object o) {
    return toBoolean(o, Boolean.FALSE);
  }
  
  //
  // BYTE
  //
  
  public static byte toByte(Object o, byte defaultValue) {
    try {
      Objects.requireNonNull(o);
      if (o instanceof Number) {
        return ((Number) o).byteValue();
      } else {
        String str = String.valueOf(o);
        return Byte.parseByte(str);
      }
    } catch (Throwable t) {
      return defaultValue;
    }
  }
  
  public static byte toByte(Object o) {
    return toByte(o, (byte) 0);
  }
  
  //
  // CHARACTER
  //
  
  public static char toCharacter(Object o, char defaultValue) {
    try {
      Objects.requireNonNull(o);
      if (o instanceof Character) {
        return (Character) o;
      } else {
        String str = String.valueOf(o);
        return str.charAt(0);
      }
    } catch (Throwable t) {
      return defaultValue;
    }
  }
  
  public static char toCharacter(Object o) {
    return toCharacter(o, '\u0000');
  }
  
  //
  // DOUBLE
  //
  
  public static double toDouble(Object o, double defaultValue) {
    try {
      Objects.requireNonNull(o);
      if (o instanceof Number) {
        return ((Number) o).doubleValue();
      } else {
        String str = String.valueOf(o);
        return Double.parseDouble(str);
      }
    } catch (Throwable t) {
      return defaultValue;
    }
  }
  
  public static double toDouble(Object o) {
    return toDouble(o, 0.D);
  }
  
  //
  // FLOAT
  //
  
  public static float toFloat(Object o, float defaultValue) {
    try {
      Objects.requireNonNull(o);
      if (o instanceof Number) {
        return ((Number) o).floatValue();
      } else {
        String str = String.valueOf(o);
        return Float.parseFloat(str);
      }
    } catch (Throwable t) {
      return defaultValue;
    }
  }
  
  public static float toFloat(Object o) {
    return toFloat(o, 0.f);
  }
  
  //
  // INTEGER
  //
  
  public static int toInteger(Object o, int defaultValue) {
    try {
      Objects.requireNonNull(o);
      if (o instanceof Number) {
        return ((Number) o).intValue();
      } else {
        return Integer.valueOf(String.valueOf(o));
      }
    } catch (Throwable t) {
      return defaultValue;
    }
  }
  
  public static int toInteger(Object o) {
    return toInteger(o, 0);
  }
  
  //
  // LONG
  //
  
  public static long toLong(Object o, long defaultValue) {
    try {
      Objects.requireNonNull(o);
      if (o instanceof Number) {
        return ((Number) o).longValue();
      } else {
        String str = String.valueOf(o);
        return Long.parseLong(str);
      }
    } catch (Throwable t) {
      return defaultValue;
    }
  }
  
  public static long toLong(Object o) {
    return toLong(o, 0L);
  }
  
  //
  // SHORT
  //
  
  public static short toShort(Object o, short defaultValue) {
    try {
      Objects.requireNonNull(o);
      if (o instanceof Number) {
        return ((Number) o).shortValue();
      } else {
        String str = String.valueOf(o);
        return Short.parseShort(str);
      }
    } catch (Throwable t) {
      return defaultValue;
    }
  }
  
  public static short toShort(Object o) {
    return toShort(o, (short) 0);
  }
  
  //
  // STRING
  //
  
  public static String toString(Object o) {
    return String.valueOf(o);
  }
}
