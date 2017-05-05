package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmHelper;
import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.transforming.ClassTransformer;
import com.matt.forgehax.asm.helper.transforming.Inject;
import com.matt.forgehax.asm.helper.transforming.MethodTransformer;
import com.matt.forgehax.asm.helper.transforming.RegisterPatch;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class VertexBufferPatch extends ClassTransformer {
    public final AsmMethod PUT_COLOR_MULTIPLIER = new AsmMethod()
            .setName("putColorMultiplier")
            .setObfuscatedName("a")
            .setArgumentTypes(float.class, float.class, float.class, int.class)
            .setReturnType(void.class)
            .setHooks(NAMES.ON_COLOR_MULTIPLIER);

    public VertexBufferPatch() {
        super("net/minecraft/client/renderer/VertexBuffer");
    }

    @RegisterPatch
    private class PutColorMultiplier extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return PUT_COLOR_MULTIPLIER;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode preNode = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    INVOKESTATIC, GETSTATIC, IF_ACMPNE,
                    0x00, 0x00,
                    ILOAD, SIPUSH, IAND, I2F, FLOAD, FMUL, F2I, ISTORE
            }, "xxx??xxxxxxxx");
            AbstractInsnNode postNode = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ALOAD, GETFIELD, ILOAD, ILOAD, INVOKEVIRTUAL, POP
            }, "xxxxxx");

            Objects.requireNonNull(preNode, "Find pattern failed for preNode");
            Objects.requireNonNull(postNode, "Find pattern failed for postNode");

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

            main.instructions.insertBefore(preNode, insnPre);
            main.instructions.insertBefore(postNode, endJump);
        }
    }
}
