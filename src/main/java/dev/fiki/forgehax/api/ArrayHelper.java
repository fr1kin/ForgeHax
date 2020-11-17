package dev.fiki.forgehax.api;

/**
 * Created on 7/19/2017 by fr1kin
 */
public class ArrayHelper {
  
  public static <T> T getOrDefault(T[] array, int index, T defaultValue) {
    return isInRange(array, index) ? array[index] : defaultValue;
  }
  
  public static <T> boolean isInRange(T[] array, int index) {
    return array != null && index >= 0 && index < array.length;
  }
}
