package com.matt.forgehax.mods;

/**
 * Created on 5/13/2017 by fr1kin
 */
public class RefreshBlockListMod extends ToggleMod {
    public RefreshBlockListMod() {
        super("RefreshBlockESP", false, "Refreshes the block ESP list");
        setHidden(true);
    }

    @Override
    public void onEnabled() {
        BlockEspMod.options.read();
    }

    @Override
    public void onDisabled() {
        BlockEspMod.options.read();
    }
}
