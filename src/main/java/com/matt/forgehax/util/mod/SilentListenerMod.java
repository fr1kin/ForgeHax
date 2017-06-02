package com.matt.forgehax.util.mod;

/**
 * Created on 6/1/2017 by fr1kin
 */

/**
 * Mod runs silently registered as an event listener
 */
public class SilentListenerMod extends SilentMod {
    public SilentListenerMod(String name, String desc) {
        super(name, desc);
    }

    @Override
    public void startup() {
        register();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
