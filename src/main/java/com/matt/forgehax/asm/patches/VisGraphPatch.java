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

public class VisGraphPatch extends ClassTransformer {
    public final AsmMethod COMPUTE_VISIBILITY = new AsmMethod()
            .setName("computeVisibility")
            .setObfuscatedName("a")
            .setArgumentTypes()
            .setReturnType(NAMES.SETVISIBILITY)
            .setHooks(NAMES.ON_COMPUTE_VISIBILITY);

    public VisGraphPatch() {
        super("net/minecraft/client/renderer/chunk/VisGraph");
    }

    @RegisterPatch
    private class ComputeVisibility extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return COMPUTE_VISIBILITY;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode node = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ALOAD, ARETURN
            }, "xx");

            Objects.requireNonNull(node, "Find pattern failed for node");

            InsnList insnPre = new InsnList();
            insnPre.add(new VarInsnNode(ALOAD, 0));
            insnPre.add(new VarInsnNode(ALOAD, 1));
            insnPre.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_COMPUTE_VISIBILITY.getParentClass().getRuntimeName(),
                    NAMES.ON_COMPUTE_VISIBILITY.getRuntimeName(),
                    NAMES.ON_COMPUTE_VISIBILITY.getDescriptor(),
                    false
            ));

            main.instructions.insertBefore(node, insnPre);
        }
    }
}
