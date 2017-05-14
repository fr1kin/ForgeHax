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

public class WorldPatch extends ClassTransformer {
    public final AsmMethod HANDLE_MATERIAL_ACCELERATION = new AsmMethod()
            .setName("handleMaterialAcceleration")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.AXISALIGNEDBB, NAMES.MATERIAL, NAMES.ENTITY)
            .setReturnType(boolean.class)
            .setHooks(NAMES.ON_WATER_MOVEMENT);

    public WorldPatch() {
        super("net/minecraft/world/World");
    }

    @RegisterMethodTransformer
    private class HandleMaterialAcceleration extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return HANDLE_MATERIAL_ACCELERATION;
        }

        @Inject
        public void inject(MethodNode method) {
            AbstractInsnNode preNode = AsmHelper.findPattern(method.instructions.getFirst(), new int[] {
                    ALOAD, INVOKEVIRTUAL, ASTORE,
                    0x00, 0x00,
                    LDC, DSTORE,
                    0x00, 0x00,
                    ALOAD, DUP, GETFIELD, ALOAD, GETFIELD, LDC, DMUL, DADD, PUTFIELD
            }, "xxx??xx??xxxxxxxxx");
            AbstractInsnNode postNode = AsmHelper.findPattern(method.instructions.getFirst(), new int[] {
                    ILOAD, IRETURN
            }, "xx");

            Objects.requireNonNull(preNode, "Find pattern failed for preNode");
            Objects.requireNonNull(postNode, "Find pattern failed for postNode");

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
        }
    }
}
