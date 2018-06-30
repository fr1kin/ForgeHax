package com.matt.forgehax.asm.asmlib;

import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderBoat;
import net.minecraft.entity.item.EntityBoat;

@Transformer(RenderBoat.class)
public class TestPatch {

    public TestPatch() {
        System.out.println("TestPatch meme");
    }

    @Inject(name = "doRender", args = {EntityBoat.class, double.class, double.class, double.class, float.class, float.class})
    public void inject(AsmMethod method) {
        method.run(() -> System.out.println("Rendering a boat!"));
    }
}