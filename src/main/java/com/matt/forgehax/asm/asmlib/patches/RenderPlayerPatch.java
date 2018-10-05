package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.events.replacementhooks.RenderPlayerEvent;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

@Transformer(RenderPlayer.class)
public class RenderPlayerPatch {

    @Inject(name = "doRender", args = {AbstractClientPlayer.class, double.class, double.class, double.class, float.class, float.class})
    public void onRender(AsmMethod method) {
        method.visitInsn(new VarInsnNode(ALOAD, 0)); // this
        method.visitInsn(new VarInsnNode(ALOAD, 1)); // player
        method.visitInsn(new VarInsnNode(FLOAD, 9)); // partialTicks
        method.visitInsn(new VarInsnNode(DLOAD, 2)); // x
        method.visitInsn(new VarInsnNode(DLOAD, 4)); // y
        method.visitInsn(new VarInsnNode(DLOAD, 6)); // z
        method.<RenderPlayerPredicate>invoke((render, player, partialTicks, x, y, z) ->
                ForgeHax.EVENT_BUS.post(new RenderPlayerEvent.Pre(render, player, partialTicks, x, y, z))
        );
        method.returnIf(true);
    }

    private interface RenderPlayerPredicate {
        boolean test(RenderPlayer render, AbstractClientPlayer player, float partialTicks, double x, double y, double z);
    }
}
