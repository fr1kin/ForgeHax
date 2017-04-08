package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.ClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * Created on 3/31/2017 by fr1kin
 */
public class WorldProviderPatch extends ClassTransformer {
    public final AsmMethod HAS_NO_SKY = new AsmMethod()
            .setName("hasNoSky")
            .setObfuscatedName("n")
            .setArgumentTypes()
            .setReturnType(boolean.class);

    @Override
    public boolean onTransformMethod(MethodNode method) {
        if(method.name.equals(HAS_NO_SKY.getRuntimeName()) &&
                method.desc.equals(HAS_NO_SKY.getDescriptor())) {
            updatePatchedMethods(handleHasNoSkyPatch(method));
            return true;
        } else return false;
    }

    private boolean handleHasNoSkyPatch(MethodNode method) {
        AbstractInsnNode returnNode = findPattern("hasNoSky", "returnNode",
                method.instructions.getFirst(), new int[] {Opcodes.IRETURN}, "x");

        if(returnNode != null) {

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.HAS_NO_SKY.getParentClass().getRuntimeName(),
                    NAMES.HAS_NO_SKY.getRuntimeName(),
                    NAMES.HAS_NO_SKY.getDescriptor(),
                    false
            ));

            method.instructions.insertBefore(returnNode, insnList);

            return true;
        } else return false;
    }
}
