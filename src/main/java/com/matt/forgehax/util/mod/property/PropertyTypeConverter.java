package com.matt.forgehax.util.mod.property;

import com.matt.forgehax.util.jopt.SafeConverter;
import net.minecraftforge.common.config.Property;

/**
 * Created on 5/15/2017 by fr1kin
 */
public class PropertyTypeConverter {
    public static String getConvertedString(Property property, String to) {
        switch (property.getType()) {
            case BOOLEAN:
                return String.valueOf(SafeConverter.toBoolean(to));
            default:
                return to;
        }
    }
}
