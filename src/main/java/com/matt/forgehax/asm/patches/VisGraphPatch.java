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

public class VisGraphPatch extends ClassTransformer {
    public final AsmMethod SET_OPAQUE_CUBE = new AsmMethod()
            .setName("setOpaqueCube")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.BLOCKPOS)
            .setReturnType(void.class);

    public final AsmMethod COMPUTE_VISIBILITY = new AsmMethod()
            .setName("computeVisibility")
            .setObfuscatedName("a")
            .setArgumentTypes()
            .setReturnType(NAMES.SETVISIBILITY);

    public VisGraphPatch() {
        super("net/minecraft/client/renderer/chunk/VisGraph");
    }

    @RegisterMethodTransformer
    private class SetOpaqueCube extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return SET_OPAQUE_CUBE;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode top = main.instructions.getFirst();
            AbstractInsnNode bottom = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    RETURN
            }, "x");

            Objects.requireNonNull(top, "Find pattern failed for top");
            Objects.requireNonNull(bottom, "Find pattern failed for bottom");

            LabelNode cancelNode = new LabelNode();

            InsnList insnList = new InsnList();
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.SHOULD_DISABLE_CULLING.getParentClass().getRuntimeName(),
                    NAMES.SHOULD_DISABLE_CULLING.getRuntimeName(),
                    NAMES.SHOULD_DISABLE_CULLING.getDescriptor(),
                    false
            ));
            insnList.add(new JumpInsnNode(IFNE, cancelNode));

            main.instructions.insertBefore(top, insnList);
            main.instructions.insertBefore(bottom, cancelNode);
        }
    }

    @RegisterMethodTransformer
    private class ComputeVisibility extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return COMPUTE_VISIBILITY;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode node = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    SIPUSH, IF_ICMPGE
            }, "xx");

            Objects.requireNonNull(node, "Find pattern failed for node");

            // gets opcode IF_ICMPGE
            JumpInsnNode greaterThanJump = (JumpInsnNode)node.getNext();
            LabelNode nextIfStatement = greaterThanJump.label;
            LabelNode orLabel = new LabelNode();

            // remove IF_ICMPGE
            main.instructions.remove(greaterThanJump);

            InsnList insnList = new InsnList();
            insnList.add(new JumpInsnNode(IF_ICMPLT, orLabel));
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.SHOULD_DISABLE_CULLING.getParentClass().getRuntimeName(),
                    NAMES.SHOULD_DISABLE_CULLING.getRuntimeName(),
                    NAMES.SHOULD_DISABLE_CULLING.getDescriptor(),
                    false
            ));
            insnList.add(new JumpInsnNode(IFEQ, nextIfStatement));
            insnList.add(orLabel);

            main.instructions.insert(node, insnList);
        }
    }
}
