package com.matt.forgehax.asm2;

import com.fr1kin.asmhelper.types.ASMClass;
import com.fr1kin.asmhelper.types.ASMMethod;

/**
 * Created on 1/17/2017 by fr1kin
 */
public interface ConstantHooks {
    //
    // classes
    //
    ASMClass CLASS_METHODHOOKS = ASMClass.getOrCreateClass("com/matt/forgehax/asm2/MethodHooks");

    //
    // methods
    //

    ASMMethod METHOD_HOOK_ONHURTCAMEFFECT = CLASS_METHODHOOKS.childMethod("onHurtcamEffect", true,
            boolean.class,
            int.class, ConstantMc.CLASS_ENTITYRENDERER, float.class
    );

    //
    // fields
    //
}
