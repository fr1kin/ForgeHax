package com.matt.forgehax.util.mod;

import com.google.common.collect.Lists;
import com.matt.forgehax.mods.BaseMod;
import net.minecraftforge.common.config.Property;

import java.util.List;

public class ModPropertyList {
    private static final Property NULL_PROPERTY = new Property("null", Boolean.toString(false), Property.Type.BOOLEAN);

    private final List<Property> properties = Lists.newArrayList();

    public ModPropertyList(BaseMod mod) {
        for(ModProperty prop : mod.getProperties()) properties.add(prop.property);
    }

    public Property getProperty(String name) {
        for(Property property : properties) if(property.getName().equals(name))
            return property;
        return NULL_PROPERTY;
    }
}
