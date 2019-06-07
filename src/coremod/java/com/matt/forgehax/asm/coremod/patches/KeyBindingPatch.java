package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Objects;
import java.util.Set;

public class KeyBindingPatch {

    @RegisterTransformer
    public static class IsKeyDown implements Transformer<MethodNode> {
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.KeyBinding_isKeyDown);
        }

        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            AbstractInsnNode node =
                ASMHelper.findPattern(
                    main.instructions.getFirst(), new int[] {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.IFEQ}, "xxx");

            Objects.requireNonNull(node, "Find pattern failed for getfield node");

            // Delete forge code
            AbstractInsnNode iteratorNode =
                node.getNext().getNext(); // set the iterator to the IFEQ instruction
            while (iteratorNode.getOpcode() != Opcodes.IRETURN) {
                iteratorNode = iteratorNode.getNext();
                main.instructions.remove(iteratorNode.getPrevious());
            }

            return main;
        }


    }
}
