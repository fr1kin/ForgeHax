package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmHelper;
import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.ClassTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class EntityRendererPatch extends ClassTransformer {
    public final AsmMethod HURTCAMERAEFFECT = new AsmMethod()
            .setName("hurtCameraEffect")
            .setObfuscatedName("d")
            .setArgumentTypes(float.class)
            .setReturnType(void.class)
            .setHooks(NAMES.ON_HURTCAMEFFECT);

    public EntityRendererPatch() {
        registerHook(HURTCAMERAEFFECT);
    }

    @Override
    public boolean onTransformMethod(MethodNode method) {
        if(method.name.equals(HURTCAMERAEFFECT.getRuntimeName()) &&
                method.desc.equals(HURTCAMERAEFFECT.getDescriptor())) {
            updatePatchedMethods(hurtCameraEffectPatch(method));
            return true;
        }
        return false;
    }

    private final int[] hurtCameraEffectPreSignature = {
            ALOAD, GETFIELD, INVOKEVIRTUAL, INSTANCEOF, IFEQ
    };

    private final int[] hurtCameraEffectPostSignature = {
            RETURN
    };

    private boolean hurtCameraEffectPatch(MethodNode method) {
        AbstractInsnNode preNode = null, postNode = null;
        try {
            preNode = AsmHelper.findPattern(method.instructions.getFirst(),
                    hurtCameraEffectPreSignature, "xxxxx");
        } catch (Exception e) {
            log("hurtCameraEffect", "preNode error: %s\n", e.getMessage());
        }
        try {
            postNode = AsmHelper.findPattern(method.instructions.getFirst(),
                    hurtCameraEffectPostSignature, "x");
        } catch (Exception e) {
            log("hurtCameraEffect", "postNode error: %s\n", e.getMessage());
        }
        if(preNode != null && postNode != null) {
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

            method.instructions.insertBefore(preNode, insnPre);
            method.instructions.insertBefore(postNode, endJump);
            return true;
        } else return false;
    }
}
