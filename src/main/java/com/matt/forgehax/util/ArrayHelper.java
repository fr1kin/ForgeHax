package com.matt.forgehax.util;

/**
 * Created on 7/19/2017 by fr1kin
 */
public class ArrayHelper {
    public static <T> T getOrDefault(T[] array, int index, T defaultValue) {
        if(array != null && index >= 0 && index < array.length)
            return array[index];
        else
            return defaultValue;
    }
}
