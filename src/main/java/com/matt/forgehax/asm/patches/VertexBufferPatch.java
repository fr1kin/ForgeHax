package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.ClassTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class VertexBufferPatch extends ClassTransformer {
    public final AsmMethod PUT_COLOR_MULTIPLIER = new AsmMethod()
            .setName("putColorMultiplier")
            .setObfuscatedName("a")
            .setArgumentTypes(float.class, float.class, float.class, int.class)
            .setReturnType(void.class)
            .setHooks(NAMES.ON_COLOR_MULTIPLIER);

    public VertexBufferPatch() {
        registerHook(PUT_COLOR_MULTIPLIER);
    }

    @Override
    public boolean onTransformMethod(MethodNode method) {
        if(method.name.equals(PUT_COLOR_MULTIPLIER.getRuntimeName()) &&
                method.desc.equals(PUT_COLOR_MULTIPLIER.getDescriptor())) {
            updatePatchedMethods(putColorMultiplierPatch(method));
            return true;
        } else return false;
    }

    private final int[] putColorMultiplierPreNode = {
            INVOKESTATIC, GETSTATIC, IF_ACMPNE,
            0x00, 0x00,
            ILOAD, SIPUSH, IAND, I2F, FLOAD, FMUL, F2I, ISTORE
    };

    private final int[] putColorMultiplierPostNode = {
            ALOAD, GETFIELD, ILOAD, ILOAD, INVOKEVIRTUAL, POP
    };

    private boolean putColorMultiplierPatch(MethodNode method) {
        AbstractInsnNode preNode = findPattern("putColorMultiplier", "preNode", method.instructions.getFirst(),
                putColorMultiplierPreNode, "xxx??xxxxxxxx");
        AbstractInsnNode postNode = findPattern("putColorMultiplier", "postNode", method.instructions.getFirst(),
                putColorMultiplierPostNode, "xxxxxx");
        if(preNode != null && postNode != null) {
            LabelNode endJump = new LabelNode();

            InsnList insnPre = new InsnList();
            insnPre.add(new InsnNode(ICONST_1));
            insnPre.add(new IntInsnNode(NEWARRAY, T_BOOLEAN));
            insnPre.add(new InsnNode(DUP));
            insnPre.add(new InsnNode(ICONST_0));
            insnPre.add(new InsnNode(ICONST_0));
            insnPre.add(new InsnNode(BASTORE));
            insnPre.add(new VarInsnNode(ASTORE, 10));
            insnPre.add(new VarInsnNode(FLOAD, 1));
            insnPre.add(new VarInsnNode(FLOAD, 2));
            insnPre.add(new VarInsnNode(FLOAD, 3));
            insnPre.add(new VarInsnNode(ILOAD, 6));
            insnPre.add(new VarInsnNode(ALOAD, 10));
            insnPre.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_COLOR_MULTIPLIER.getParentClass().getRuntimeName(),
                    NAMES.ON_COLOR_MULTIPLIER.getRuntimeName(),
                    NAMES.ON_COLOR_MULTIPLIER.getDescriptor(),
                    false
            ));
            insnPre.add(new VarInsnNode(ISTORE, 6));
            insnPre.add(new VarInsnNode(ALOAD, 10));
            insnPre.add(new InsnNode(ICONST_0));
            insnPre.add(new InsnNode(BALOAD));
            insnPre.add(new JumpInsnNode(IFNE, endJump));

            method.instructions.insertBefore(preNode, insnPre);
            method.instructions.insertBefore(postNode, endJump);
            return true;
        } else return false;
    }
}
