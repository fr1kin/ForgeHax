package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderWorker;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(ChunkRenderWorker.class)
public class ChunkRenderWorkerPatch {

    @Inject(name = "freeRenderBuilder", args = {ChunkCompileTaskGenerator.class},
    description = "Add hook at the very top of the method")
    public void freeRenderBuilder(MethodNode main) {
        AbstractInsnNode node = main.instructions.getFirst();

        Objects.requireNonNull(node, "Find pattern failed for node");

        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(ALOAD, 1));
        insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onWorldRendererDeallocated));

        main.instructions.insertBefore(node, insnList);
    }
}
