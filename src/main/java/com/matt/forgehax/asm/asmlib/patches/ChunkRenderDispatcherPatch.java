package com.matt.forgehax.asm.asmlib.patches;

import com.google.common.util.concurrent.ListenableFuture;
import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(ChunkRenderDispatcher.class)
public class ChunkRenderDispatcherPatch {

    @Inject(name = "uploadChunk",
            args = {BlockRenderLayer.class, BufferBuilder.class, RenderChunk.class, CompiledChunk.class, double.class},
            ret = ListenableFuture.class,
    description = "Insert hook before buffer is uploaded"
    )
    public void uploadChunk(MethodNode main) {
        AbstractInsnNode node = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                INVOKESTATIC, IFEQ,
                0x00, 0x00,
                ALOAD,
        }, "xx??x");

        Objects.requireNonNull(node, "Find pattern failed for node");

        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(ALOAD, 3));
        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onChunkUploaded));

        main.instructions.insertBefore(node, insnList);
    }
}
