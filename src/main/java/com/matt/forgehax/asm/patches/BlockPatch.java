package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.ClassTransformer;
import org.objectweb.asm.tree.*;

import java.util.List;

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

    public final AsmMethod ADD_COLLISION_BOX_TO_LIST = new AsmMethod()
            .setName("addCollisionBoxToList")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.IBLOCKSTATE, NAMES.WORLD, NAMES.BLOCKPOS, NAMES.AXISALIGNEDBB, List.class, NAMES.ENTITY, boolean.class)
            .setReturnType(void.class);

    public BlockPatch() {
        registerHook(CAN_RENDER_IN_LAYER);
    }

    @Override
    public boolean onTransformMethod(MethodNode method) {
        if(method.name.equals(CAN_RENDER_IN_LAYER.getRuntimeName()) &&
                method.desc.equals(CAN_RENDER_IN_LAYER.getDescriptor())) {
            updatePatchedMethods(canRenderinLayerPatch(method));
            return true;
        } else if(method.name.equals(ADD_COLLISION_BOX_TO_LIST.getRuntimeName()) &&
                method.desc.equals(ADD_COLLISION_BOX_TO_LIST.getDescriptor())) {
            updatePatchedMethods(addCollisionBoxToListPatch(method));
            return true;
        }
        return false;
    }

    private final int[] canRenderBlockLayerPreSig = {
            INVOKEVIRTUAL
    };

    private final int[] canRenderBlockLayerPostSig = {
            IRETURN
    };

    private boolean canRenderinLayerPatch(MethodNode node) {
        boolean ret = false;
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
            ret = true;
        }
        return ret;
    }

    private boolean addCollisionBoxToListPatch(MethodNode methodNode) {
        AbstractInsnNode pos = findPattern("addCollisionBoxToList", "pos",
                methodNode.instructions.getFirst(), new int[] {ALOAD}, "x");
        if(pos != null) {
            InsnList insnPre = new InsnList();
            insnPre.add(new VarInsnNode(ALOAD, 0)); //this
            insnPre.add(new VarInsnNode(ALOAD, 1)); //state
            insnPre.add(new VarInsnNode(ALOAD, 2)); //world
            insnPre.add(new VarInsnNode(ALOAD, 5)); //list
            insnPre.add(new VarInsnNode(ALOAD, 3)); //pos
            insnPre.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_BLOCK_ADD_COLLISION.getParentClass().getRuntimeName(),
                    NAMES.ON_BLOCK_ADD_COLLISION.getRuntimeName(),
                    NAMES.ON_BLOCK_ADD_COLLISION.getDescriptor(),
                    false
            ));

            methodNode.instructions.insert(pos, insnPre);
            return true;
        }
        return false;
    }
}
