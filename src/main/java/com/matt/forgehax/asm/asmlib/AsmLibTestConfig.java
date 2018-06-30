package com.matt.forgehax.asm.asmlib;

import net.futureclient.asm.config.Config;

public class AsmLibTestConfig extends Config {

    public AsmLibTestConfig() {
        super("ForgehaxAsmLibConfig");
        this.addClassTransformer("com.matt.forgehax.asm.asmlib.TestMainPatch");
        this.addClassTransformer("com.matt.forgehax.asm.asmlib.TestPatch");
        System.out.println("Created AsmLibTestConfig");
    }

}
