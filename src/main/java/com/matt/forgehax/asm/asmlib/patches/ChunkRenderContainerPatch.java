package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.renderer.ChunkRenderContainer;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(ChunkRenderContainer.class)
public class ChunkRenderContainerPatch {

    @Inject(name = "addRenderChunk", args = {RenderChunk.class, BlockRenderLayer.class})
    public void addRenderChunk(MethodNode main) {
        AbstractInsnNode node = main.instructions.getFirst();

        Objects.requireNonNull(node, "Find pattern failed for node");

        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(ALOAD, 1));
        insnList.add(new VarInsnNode(ALOAD, 2));
        insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onAddRenderChunk));

        main.instructions.insertBefore(node, insnList);
    }
}
