package com.matt.forgehax.asm2;

import com.fr1kin.asmhelper.types.ASMClass;
import com.fr1kin.asmhelper.types.ASMMethod;

/**
 * Created on 1/28/2017 by fr1kin
 */
public interface ConstantOverrides {
    //
    // classes
    //
    ASMClass CLASS_METHODOVERRIDES = ASMClass.getOrCreateClass("com/matt/forgehax/asm2/MethodOverrides");

    //
    // methods
    //
    ASMMethod METHOD_OVERRIDE_CANRENDERINLAYER = CLASS_METHODOVERRIDES.childMethod("canRenderInLayerOverride", true,
            boolean.class,
            String.class, ConstantMc.CLASS_BLOCK, ConstantMc.CLASS_IBLOCKSTATE, ConstantMc.CLASS_BLOCKRENDERLAYER
    );

    //
    // fields
    //
}
