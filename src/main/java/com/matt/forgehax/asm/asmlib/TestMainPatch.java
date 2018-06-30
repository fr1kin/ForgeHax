package com.matt.forgehax.asm.asmlib;

import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.main.Main;

@Transformer(value = Main.class, remap = false)
public class TestMainPatch {

    @Inject(name = "main", args = {String[].class})
    public void inject(AsmMethod method) {
        method.run(() -> System.out.println("Injecting into main"));
    }
}