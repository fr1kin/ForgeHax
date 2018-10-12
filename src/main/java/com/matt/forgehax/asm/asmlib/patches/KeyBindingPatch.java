package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.settings.KeyBinding;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(KeyBinding.class)
public class KeyBindingPatch {

    @Inject(name = "isKeyDown", ret = boolean.class,
    description = "Shut down forge's shit for GuiMove"
    )
    public void isKeyDown(MethodNode main) {
        AbstractInsnNode node = ASMHelper.findPattern(main.instructions.getFirst(), new int[]{
                ALOAD, GETFIELD, IFEQ
        }, "xxx");

        Objects.requireNonNull(node, "Find pattern failed for getfield node, probably not forge");

        // Delete forge code
        AbstractInsnNode iteratorNode = node.getNext().getNext(); // set the iterator to the IFEQ instruction
        while (iteratorNode.getOpcode() != IRETURN) {
            iteratorNode = iteratorNode.getNext();
            main.instructions.remove(iteratorNode.getPrevious());
        }

    }
}
