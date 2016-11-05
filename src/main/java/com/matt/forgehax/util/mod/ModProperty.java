package com.matt.forgehax.util.mod;

import net.minecraftforge.common.config.Property;

/**
 * Class used to detect if a property changes, because for some reason forge
 * doesn't allow you to call their method to reset hasChanged()...
 */
public class ModProperty {
    public Property property;

    private String lastValue = "";

    public ModProperty(Property property) {
        this.property = property;
        update();
    }

    /**
     * Updates last value to the properties new value
     */
    public void update() {
        lastValue = property.getString();
    }

    /**
     * Checks if the property has changed values
     */
    public boolean hasChanged() {
        return !property.getString().equals(lastValue);
    }
}
