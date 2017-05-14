package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmHelper;
import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.transforming.ClassTransformer;
import com.matt.forgehax.asm.helper.transforming.Inject;
import com.matt.forgehax.asm.helper.transforming.MethodTransformer;
import com.matt.forgehax.asm.helper.transforming.RegisterMethodTransformer;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class EntityRendererPatch extends ClassTransformer {
    public final AsmMethod HURTCAMERAEFFECT = new AsmMethod()
            .setName("hurtCameraEffect")
            .setObfuscatedName("d")
            .setArgumentTypes(float.class)
            .setReturnType(void.class)
            .setHooks(NAMES.ON_HURTCAMEFFECT);

    public EntityRendererPatch() {
        super("net/minecraft/client/renderer/EntityRenderer");
    }

    @RegisterMethodTransformer
    private class HurtCameraEffect extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return HURTCAMERAEFFECT;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode preNode = main.instructions.getFirst();
            AbstractInsnNode postNode = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {RETURN}, "x");

            Objects.requireNonNull(preNode, "Find pattern failed for preNode");
            Objects.requireNonNull(postNode, "Find pattern failed for postNode");

            LabelNode endJump = new LabelNode();

            InsnList insnPre = new InsnList();
            insnPre.add(new VarInsnNode(FLOAD, 1));
            insnPre.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_HURTCAMEFFECT.getParentClass().getRuntimeName(),
                    NAMES.ON_HURTCAMEFFECT.getRuntimeName(),
                    NAMES.ON_HURTCAMEFFECT.getDescriptor(),
                    false
            ));
            insnPre.add(new JumpInsnNode(IFNE, endJump));

            main.instructions.insertBefore(preNode, insnPre);
            main.instructions.insertBefore(postNode, endJump);
        }
    }
}
