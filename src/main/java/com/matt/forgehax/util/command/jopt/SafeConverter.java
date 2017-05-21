package com.matt.forgehax.util.command.jopt;

import java.util.Objects;

/**
 * Created on 5/18/2017 by fr1kin
 */
public class SafeConverter {
    public static int toInteger(Object o, int defaultValue) {
        try {
            Objects.requireNonNull(o);
            return Integer.valueOf(String.valueOf(o));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int toInteger(Object o) {
        return toInteger(o, 0);
    }
}
