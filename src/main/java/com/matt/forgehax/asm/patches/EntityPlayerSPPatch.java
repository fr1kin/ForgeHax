package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.ClassTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created on 11/13/2016 by fr1kin
 */
public class EntityPlayerSPPatch extends ClassTransformer {
    public final AsmMethod APPLY_ENTITY_COLLISION = new AsmMethod()
            .setName("onLivingUpdate")
            .setObfuscatedName("n")
            .setArgumentTypes()
            .setReturnType(void.class)
            .setHooks();

    public EntityPlayerSPPatch() {
        registerHook(APPLY_ENTITY_COLLISION);
    }

    @Override
    public boolean onTransformMethod(MethodNode method) {
        if(method.name.equals(APPLY_ENTITY_COLLISION.getRuntimeName()) &&
                method.desc.equals(APPLY_ENTITY_COLLISION.getDescriptor())) {
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
            insnList.add(new JumpInsnNode(IFEQ, jumpTo));

            method.instructions.insert(applySlowdownSpeedNode, insnList);
            return true;
        } else return false;
    }
}
