package com.matt.forgehax.asm.utils.asmtype.builders;

import com.matt.forgehax.asm.utils.asmtype.ASMClass;

/**
 * Created on 5/27/2017 by fr1kin
 */
public class ASMBuilders {
    public static ASMClassBuilder newClassBuilder() {
        return new ASMClassBuilder();
    }

    public static ASMMethodBuilder newMethodBuilder() {
        return new ASMMethodBuilder();
    }

    public static ASMFieldBuilder newFieldBuilder() {
        return new ASMFieldBuilder();
    }

    public static ParameterBuilder newParameterBuilder() {
        return new ParameterBuilder();
    }
}
