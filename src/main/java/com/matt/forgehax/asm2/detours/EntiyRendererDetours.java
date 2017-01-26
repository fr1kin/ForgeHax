package com.matt.forgehax.asm2.detours;

import com.fr1kin.asmhelper.detours.Detours;
import com.fr1kin.asmhelper.utils.locator.Locators;

/**
 * Created on 1/17/2017 by fr1kin
 */
public class EntiyRendererDetours extends ClassDetour {
    public EntiyRendererDetours() {
        super(MCP.CLASS_ENTITYRENDERER);
    }

    @Override
    protected void initialize() {
        register(Detours.newCancellablePrePosDetour(MCP.METHOD_ENTITYRENDERER_HURTCAM_EFFECT, HOOKS.METHOD_ONHURTCAMEFFECT,
                Locators::firstLabelNode,
                Locators::returnNode
        ));
    }
}
