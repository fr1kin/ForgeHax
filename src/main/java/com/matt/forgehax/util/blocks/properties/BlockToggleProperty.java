package com.matt.forgehax.util.blocks.properties;

import com.google.gson.JsonObject;

/**
 * Created on 5/24/2017 by fr1kin
 */
public class BlockToggleProperty implements IBlockProperty {
    private static final String HEADING = "enabled";

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public void toggle() {
        if(enabled)
            disable();
        else
            enable();
    }

    @Override
    public void serialize(JsonObject head) {
        head.addProperty(HEADING, enabled);
    }

    @Override
    public void deserialize(JsonObject head) {
        if(head.has(HEADING)) try {
            enabled = head.get(HEADING).getAsBoolean();
        } catch (Exception e) {
            ;
        }
    }

    @Override
    public String toString() {
        return String.format("%s=%s", HEADING, Boolean.toString(enabled));
    }
}
