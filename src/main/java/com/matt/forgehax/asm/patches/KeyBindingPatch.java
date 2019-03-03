package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.transformer.RegisterTransformer;
import com.matt.forgehax.asm.transformer.Transformer;
import com.matt.forgehax.asm.utils.ASMHelper;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Objects;
import java.util.Set;

public class KeyBindingPatch {

    @RegisterTransformer
    public static class IsKeyDown implements Transformer<MethodNode> {
        @Override
        public Set<Target> targets() {
            return ASMHelper.getTargetSet(Methods.KeyBinding_isKeyDown);
        }

        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            AbstractInsnNode node =
                ASMHelper.findPattern(
                    main.instructions.getFirst(), new int[] {ALOAD, GETFIELD, IFEQ}, "xxx");

            Objects.requireNonNull(node, "Find pattern failed for getfield node");

            // Delete forge code
            AbstractInsnNode iteratorNode =
                node.getNext().getNext(); // set the iterator to the IFEQ instruction
            while (iteratorNode.getOpcode() != IRETURN) {
                iteratorNode = iteratorNode.getNext();
                main.instructions.remove(iteratorNode.getPrevious());
            }

            return main;
        }


    }
}
