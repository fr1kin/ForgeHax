package com.matt.forgehax.asm2.detours;

import com.fr1kin.asmhelper.detours.Detours;
import com.fr1kin.asmhelper.utils.locator.Locators;

/**
 * Created on 1/17/2017 by fr1kin
 */
public class EntityRendererDetours extends ClassDetour {
    public EntityRendererDetours() {
        super(CLASS_ENTITYRENDERER);
    }

    @Override
    protected void initialize() {
        registerAll(Detours.newCancellablePrePostDetour(METHOD_ENTITYRENDERER_HURTCAMEFFECT, METHOD_HOOK_ONHURTCAMEFFECT,
                Locators::firstLabelNode,
                Locators::returnNode
        ));
    }
}