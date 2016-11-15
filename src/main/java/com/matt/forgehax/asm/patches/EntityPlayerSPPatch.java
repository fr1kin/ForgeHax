package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.ClassTransformer;
import org.objectweb.asm.tree.*;

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
        registerHook(ON_LIVING_UPDATE);
    }

    @Override
    public boolean onTransformMethod(MethodNode method) {
        if(method.name.equals(ON_LIVING_UPDATE.getRuntimeName()) &&
                method.desc.equals(ON_LIVING_UPDATE.getDescriptor())) {
            updatePatchedMethods(applyLivingUpdatePatch(method));
            return true;
        } else return false;
    }

    private final int[] applySlowdownSpeedSig = {
            IFNE,
            0x00, 0x00,
            ALOAD, GETFIELD, DUP, GETFIELD, LDC, FMUL, PUTFIELD
    };

    private boolean applyLivingUpdatePatch(MethodNode method) {
        AbstractInsnNode applySlowdownSpeedNode = findPattern("onLivingUpdate", "applySlowdownSpeedNode",
                method.instructions.getFirst(), applySlowdownSpeedSig, "x??xxxxxxx");
        if(applySlowdownSpeedNode != null &&
                applySlowdownSpeedNode instanceof JumpInsnNode) {
            // get label it jumps to
            LabelNode jumpTo = ((JumpInsnNode) applySlowdownSpeedNode).label;

            InsnList insnList = new InsnList();
            insnList.add(new FieldInsnNode(GETSTATIC,
                    NAMES.IS_NOSLOWDOWN_ACTIVE.getParentClass().getRuntimeName(),
                    NAMES.IS_NOSLOWDOWN_ACTIVE.getRuntimeName(),
                    NAMES.IS_NOSLOWDOWN_ACTIVE.getTypeDescriptor()
            ));// get the value of IS_NOSLOWDOWN_ACTIVE
            insnList.add(new JumpInsnNode(IFNE, jumpTo));

            method.instructions.insert(applySlowdownSpeedNode, insnList);
            return true;
        } else return false;
    }
}
