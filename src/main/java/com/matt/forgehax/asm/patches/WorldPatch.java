package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.ClassTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class WorldPatch extends ClassTransformer {
    public final AsmMethod HANDLE_MATERIAL_ACCELERATION = new AsmMethod()
            .setName("handleMaterialAcceleration")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.AXISALIGNEDBB, NAMES.MATERIAL, NAMES.ENTITY)
            .setReturnType(boolean.class)
            .setHooks(NAMES.ON_WATER_MOVEMENT);

    public WorldPatch() {
        registerHook(HANDLE_MATERIAL_ACCELERATION);
    }

    @Override
    public boolean onTransformMethod(MethodNode method) {
        if(method.name.equals(HANDLE_MATERIAL_ACCELERATION.getRuntimeName()) &&
                method.desc.equals(HANDLE_MATERIAL_ACCELERATION.getDescriptor())) {
            updatePatchedMethods(handleMaterialAccelerationPatch(method));
            return true;
        } else return false;
    }

    private final int[] handleMaterialAccelerationPreSignature = {
            ALOAD, INVOKEVIRTUAL, ASTORE,
            0x00, 0x00,
            LDC, DSTORE,
            0x00, 0x00,
            ALOAD, DUP, GETFIELD, ALOAD, GETFIELD, LDC, DMUL, DADD, PUTFIELD
    };

    private final int[] handleMaterialAccelerationPostSignature = {
            ILOAD, IRETURN
    };

    private boolean handleMaterialAccelerationPatch(MethodNode method) {
        AbstractInsnNode preNode = findPattern("handleMaterialAcceleration", "preNode", method.instructions.getFirst(),
                handleMaterialAccelerationPreSignature, "xxx??xx??xxxxxxxxx");
        AbstractInsnNode postNode = findPattern("handleMaterialAcceleration", "postNode", method.instructions.getFirst(),
                handleMaterialAccelerationPostSignature, "xx");
        if(preNode != null && postNode != null) {
            LabelNode endJump = new LabelNode();

            InsnList insnPre = new InsnList();
            insnPre.add(new VarInsnNode(ALOAD, 3));
            insnPre.add(new VarInsnNode(ALOAD, 11));
            insnPre.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_WATER_MOVEMENT.getParentClass().getRuntimeName(),
                    NAMES.ON_WATER_MOVEMENT.getRuntimeName(),
                    NAMES.ON_WATER_MOVEMENT.getDescriptor(),
                    false
            ));
            insnPre.add(new JumpInsnNode(IFNE, endJump));

            method.instructions.insertBefore(preNode, insnPre);
            method.instructions.insertBefore(postNode, endJump);
            return true;
        } else {
            return false;
        }
    }
}
