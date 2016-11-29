package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.ClassTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IRETURN;

public class BlockPatch extends ClassTransformer {
    public final AsmMethod CAN_RENDER_IN_LAYER = new AsmMethod()
            .setName("canRenderInLayer")
            .setObfuscatedName("canRenderInLayer")
            .setArgumentTypes(NAMES.IBLOCKSTATE, NAMES.BLOCK_RENDER_LAYER)
            .setReturnType(boolean.class)
            .setHooks(NAMES.ON_RENDERBLOCK_INLAYER);

    public BlockPatch() {
        registerHook(CAN_RENDER_IN_LAYER);
    }

    @Override
    public boolean onTransformMethod(MethodNode method) {
        if(method.name.equals(CAN_RENDER_IN_LAYER.getRuntimeName()) &&
                method.desc.equals(CAN_RENDER_IN_LAYER.getDescriptor())) {
            updatePatchedMethods(canRenderinLayerPatch(method));
            return true;
        } else return false;
    }

    private final int[] canRenderBlockLayerPreSig = {
            INVOKEVIRTUAL
    };

    private final int[] canRenderBlockLayerPostSig = {
            IRETURN
    };

    private boolean canRenderinLayerPatch(MethodNode node) {
        AbstractInsnNode preNode = findPattern("canRenderInLayer", "preNode",
                node.instructions.getFirst(), canRenderBlockLayerPreSig, "x");
        AbstractInsnNode postNode = findPattern("canRenderInLayer", "postNode",
                node.instructions.getFirst(), canRenderBlockLayerPostSig, "x");
        if(preNode != null && postNode != null) {
            InsnList insnPre = new InsnList();
            // starting after INVOKEVIRTUAL on Block.getBlockLayer()
            insnPre.add(new VarInsnNode(ASTORE, 3));
            insnPre.add(new VarInsnNode(ALOAD, 0));
            insnPre.add(new VarInsnNode(ALOAD, 1));
            insnPre.add(new VarInsnNode(ALOAD, 3));
            insnPre.add(new VarInsnNode(ALOAD, 2));
            insnPre.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_RENDERBLOCK_INLAYER.getParentClass().getRuntimeName(),
                    NAMES.ON_RENDERBLOCK_INLAYER.getRuntimeName(),
                    NAMES.ON_RENDERBLOCK_INLAYER.getDescriptor(),
                    false
            ));
            // now our result is on the stack

            node.instructions.insert(preNode, insnPre);
            return true;
        } else return false;
    }
}
