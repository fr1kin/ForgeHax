package com.matt.forgehax.util.mod;

/**
 * Created on 6/1/2017 by fr1kin
 */

/**
 * Mod that will be hidden and not show up in the mod list
 */
public class SilentMod extends BaseMod {
    public SilentMod(String name, String desc) {
        super(name, desc);
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void toggle() {}
}
