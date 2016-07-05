package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.ClassTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IRETURN;

public class VisGraphPatch extends ClassTransformer {
    public final AsmMethod COMPUTE_VISIBILITY = new AsmMethod()
            .setName("computeVisibility")
            .setObfuscatedName("a")
            .setArgumentTypes()
            .setReturnType(NAMES.SETVISIBILITY)
            .setHooks(NAMES.ON_COMPUTE_VISIBILITY);

    public VisGraphPatch() {
        registerHook(COMPUTE_VISIBILITY);
    }

    @Override
    public boolean onTransformMethod(MethodNode method) {
        if(method.name.equals(COMPUTE_VISIBILITY.getRuntimeName()) &&
                method.desc.equals(COMPUTE_VISIBILITY.getDescriptor())) {
            updatePatchedMethods(computeVisibilityPatch(method));
            return true;
        } else return false;
    }

    private final int[] computeVisSignature = {
            ALOAD, ARETURN
    };

    private boolean computeVisibilityPatch(MethodNode method) {
        AbstractInsnNode node = findPattern("computeVisibility", "node", method.instructions.getFirst(),
                computeVisSignature, "xx");
        if(node != null) {
            InsnList insnPre = new InsnList();
            insnPre.add(new VarInsnNode(ALOAD, 0));
            insnPre.add(new VarInsnNode(ALOAD, 1));
            insnPre.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_COMPUTE_VISIBILITY.getParentClass().getRuntimeName(),
                    NAMES.ON_COMPUTE_VISIBILITY.getRuntimeName(),
                    NAMES.ON_COMPUTE_VISIBILITY.getDescriptor(),
                    false
            ));

            method.instructions.insertBefore(node, insnPre);
            return true;
        } else return false;
    }
}
