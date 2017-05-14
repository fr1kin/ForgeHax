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

/**
 * Created on 11/13/2016 by fr1kin
 */
public class EntityPlayerSPPatch extends ClassTransformer {
    public final AsmMethod ON_LIVING_UPDATE = new AsmMethod()
            .setName("onLivingUpdate")
            .setObfuscatedName("n")
            .setArgumentTypes()
            .setReturnType(void.class)
            .setHooks();

    public EntityPlayerSPPatch() {
        super("net/minecraft/client/entity/EntityPlayerSP");
    }

    @RegisterMethodTransformer
    private class ApplyLivingUpdate extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return ON_LIVING_UPDATE;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode applySlowdownSpeedNode = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    IFNE,
                    0x00, 0x00,
                    ALOAD, GETFIELD, DUP, GETFIELD, LDC, FMUL, PUTFIELD
            }, "x??xxxxxxx");

            Objects.requireNonNull(applySlowdownSpeedNode, "Find pattern failed for applySlowdownSpeedNode");

            // get label it jumps to
            LabelNode jumpTo = ((JumpInsnNode) applySlowdownSpeedNode).label;

            InsnList insnList = new InsnList();
            insnList.add(new FieldInsnNode(GETSTATIC,
                    NAMES.IS_NOSLOWDOWN_ACTIVE.getParentClass().getRuntimeName(),
                    NAMES.IS_NOSLOWDOWN_ACTIVE.getRuntimeName(),
                    NAMES.IS_NOSLOWDOWN_ACTIVE.getTypeDescriptor()
            ));// get the value of IS_NOSLOWDOWN_ACTIVE
            insnList.add(new JumpInsnNode(IFNE, jumpTo));

            main.instructions.insert(applySlowdownSpeedNode, insnList);
        }
    }
}
