package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.ClassTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created on 11/10/2016 by fr1kin
 */
public class BlockRendererDispatcherPatch extends ClassTransformer {
    public final AsmMethod ON_RENDER_BLOCK = new AsmMethod()
            .setName("renderBlock")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.IBLOCKSTATE, NAMES.BLOCKPOS, NAMES.IBLOCKACCESS, NAMES.VERTEXBUFFER)
            .setReturnType(boolean.class)
            .setHooks(NAMES.ON_RENDER_BLOCK);

    public BlockRendererDispatcherPatch() {
        registerHook(ON_RENDER_BLOCK);
    }

    @Override
    public boolean onTransformMethod(MethodNode method) {
        if(method.name.equals(ON_RENDER_BLOCK.getRuntimeName()) &&
                method.desc.equals(ON_RENDER_BLOCK.getDescriptor())) {
            updatePatchedMethods(applyBlockRenderPatch(method));
            return true;
        } else return false;
    }

    private boolean applyBlockRenderPatch(MethodNode method) {
        AbstractInsnNode startNode = findPattern("blockRender", "startNode",
                method.instructions.getFirst(), new int[] {ALOAD, INVOKEINTERFACE}, "xx");
        if(startNode != null) {
            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 2));
            insnList.add(new VarInsnNode(ALOAD, 1));
            insnList.add(new VarInsnNode(ALOAD, 3));
            insnList.add(new VarInsnNode(ALOAD, 4));
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_RENDER_BLOCK.getParentClass().getRuntimeName(),
                    NAMES.ON_RENDER_BLOCK.getRuntimeName(),
                    NAMES.ON_RENDER_BLOCK.getDescriptor(),
                    false
            ));

            method.instructions.insertBefore(startNode, insnList);
            return true;
        } else return false;
    }
}
