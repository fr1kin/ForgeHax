package com.matt.forgehax.asm2.detours;

import com.fr1kin.asmhelper.detours.Detours;

/**
 * Created on 1/29/2017 by fr1kin
 */
public class BlockDetours extends ClassDetour {
    public BlockDetours() {
        super(CLASS_BLOCK);
    }

    @Override
    protected void initialize() {
        registerAll(
                Detours.newOverrideDetour(METHOD_BLOCK_CANRENDERINLAYER, METHOD_OVERRIDE_CANRENDERINLAYER)
        );
    }
}
