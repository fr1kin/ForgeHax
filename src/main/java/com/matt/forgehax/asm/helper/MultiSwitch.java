package com.matt.forgehax.asm.helper;

/**
 * Created on 5/12/2017 by fr1kin
 */
public class MultiSwitch {
    private int level = 0;

    public void enable() {
        ++level;
    }

    public void disable() {
        --level;
    }

    public void forceDisable() {
        level = 0;
    }

    public boolean isEnabled() {
        return level > 0;
    }
}
