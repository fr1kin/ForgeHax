package com.matt.forgehax.util.command.jopt;

import joptsimple.OptionSet;

/**
 * Created on 5/18/2017 by fr1kin
 */
public class OptionHelper {
    private final OptionSet options;

    public OptionHelper(OptionSet options) {
        this.options = options;
    }

    public int getIntOrDefault(String option, int defaultValue) {
        try {
            return SafeConverter.toInteger(options.has(option) ? options.valuesOf(option).get(0) : defaultValue, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
