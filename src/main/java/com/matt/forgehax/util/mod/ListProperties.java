package com.matt.forgehax.util.mod;

import com.google.common.collect.Lists;
import net.minecraftforge.common.config.Property;

import java.util.List;

public class ListProperties {
    private static final Property NULL_PROPERTY = new Property("<null>", Boolean.toString(false), Property.Type.BOOLEAN);

    private final List<Property> properties = Lists.newArrayList();

    public Property getProperty(String name) {
        for(Property property : properties)
            if(property.getName().toLowerCase().equals(name.toLowerCase()))
                return property;
        return NULL_PROPERTY;
    }
}
