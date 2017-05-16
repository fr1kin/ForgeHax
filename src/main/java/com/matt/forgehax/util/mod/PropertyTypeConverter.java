package com.matt.forgehax.util.mod;

import net.minecraftforge.common.config.Property;

/**
 * Created on 5/15/2017 by fr1kin
 */
public class PropertyTypeConverter {
    public static String getConvertedString(Property property, String to) {
        switch (property.getType()) {
            case BOOLEAN:
                try {
                    // allows entering 0 or 1
                    return Integer.valueOf(to) == 0 ? Boolean.FALSE.toString() : Boolean.TRUE.toString();
                } catch (Exception e) {
                    // allows entering t (true), f (false), on (true), off (false)
                    if(to.matches("t|on"))
                        return Boolean.TRUE.toString();
                    else if(to.matches("f|off"))
                        return Boolean.FALSE.toString();
                }
                return Boolean.valueOf(to).toString();
            default:
                return to;
        }
    }
}
