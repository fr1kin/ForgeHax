package com.matt.forgehax.asm.patches.special;

import com.matt.forgehax.asm.helper.AsmHelper;
import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.transforming.ClassTransformer;
import com.matt.forgehax.asm.helper.transforming.Inject;
import com.matt.forgehax.asm.helper.transforming.MethodTransformer;
import com.matt.forgehax.asm.helper.transforming.RegisterMethodTransformer;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created on 5/2/2017 by fr1kin
 */
public class BaseRendererPatch extends ClassTransformer {
    public final AsmMethod SET_STRATUM_COLORS = new AsmMethod()
            .setName("setStratumColors")
            .setArgumentTypes(NAMES.JOURNYMAP_STRATUM, int.class, Integer.class, boolean.class, boolean.class, boolean.class)
            .setReturnType(void.class);

    public BaseRendererPatch() {
        super("journeymap/client/cartography/render/BaseRenderer");
    }

    @RegisterMethodTransformer
    private class SetStratumColors extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return SET_STRATUM_COLORS;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode start = main.instructions.getFirst();
            AbstractInsnNode end = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    RETURN
            }, "x");

            Objects.requireNonNull(end, "Find pattern failed for end");

            LabelNode endJump = new LabelNode();

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 0));
            insnList.add(new VarInsnNode(ALOAD, 1));
            insnList.add(new VarInsnNode(ILOAD, 2));
            insnList.add(new VarInsnNode(ALOAD, 3));
            insnList.add(new VarInsnNode(ILOAD, 4));
            insnList.add(new VarInsnNode(ILOAD, 5));
            insnList.add(new VarInsnNode(ILOAD, 6));
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.JOURNYMAP_ON_SET_STRATUM_COLOR.getParentClass().getRuntimeName(),
                    NAMES.JOURNYMAP_ON_SET_STRATUM_COLOR.getRuntimeName(),
                    NAMES.JOURNYMAP_ON_SET_STRATUM_COLOR.getDescriptor(),
                    false
            ));
            insnList.add(new JumpInsnNode(IFNE, endJump));

            main.instructions.insertBefore(end, endJump);
            main.instructions.insertBefore(start, insnList);
        }
    }
}
