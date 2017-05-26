package com.matt.forgehax.util.command.jopt;

import com.matt.forgehax.util.command.CommandLine;

import java.util.Objects;

/**
 * Created on 5/18/2017 by fr1kin
 */
public class SafeConverter {
    private static final String ACCEPTABLE_TRUE_BOOLEAN_STRINGS = CommandLine.join(new String[] {Boolean.TRUE.toString(), "t", "on", "enable", "enabled"}, "|");

    public static int toInteger(Object o, int defaultValue) {
        try {
            Objects.requireNonNull(o);
            if(o instanceof Number)
                return ((Number) o).intValue();
            else
                return Integer.valueOf(String.valueOf(o));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int toInteger(Object o) {
        return toInteger(o, 0);
    }

    public static boolean toBoolean(Object o, boolean defaultValue) {
        try {
            Objects.requireNonNull(o);
            if(o instanceof Boolean)
                return ((Boolean) o);
            else {
                String str = String.valueOf(o);
                try {
                    return Integer.valueOf(str) != 0;
                } catch (Exception e) {
                    return str.matches(ACCEPTABLE_TRUE_BOOLEAN_STRINGS);
                }
            }
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean toBoolean(Object o) {
        return toBoolean(o, false);
    }
}
