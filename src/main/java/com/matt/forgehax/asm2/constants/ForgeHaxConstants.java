package com.matt.forgehax.asm2.constants;

import com.fr1kin.asmhelper.types.ASMClass;
import com.fr1kin.asmhelper.types.ASMMethod;

/**
 * Created on 1/17/2017 by fr1kin
 */
public class ForgeHaxConstants {
    private static ForgeHaxConstants instance = null;

    public static ForgeHaxConstants getInstance() {
        return instance == null ? instance = new ForgeHaxConstants() : instance;
    }

    private final McpConstants MCP = McpConstants.getInstance();

    //
    // classes
    //

    public final ASMClass CLASS_HOOKFUNCTIONS = ASMClass.getOrCreateClass("com/matt/forgehax/ams2/HookFunctions");

    //
    // methods
    //

    public final ASMMethod METHOD_ONHURTCAMEFFECT = CLASS_HOOKFUNCTIONS.childMethod("onHurtcamEffect", true,
            boolean.class,
            MCP.CLASS_ENTITYRENDERER, float.class
    );

    //
    // fields
    //
}
