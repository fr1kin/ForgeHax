package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(VisGraph.class)
public class VisGraphPatch {

    @Inject(name = "setOpaqueCube", args = {BlockPos.class},
    description = "Add hook at the end that can override the return value"
    )
    public void setOpaqueCube(MethodNode main) {
        AbstractInsnNode top = main.instructions.getFirst();
        AbstractInsnNode bottom = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                RETURN
        }, "x");

        Objects.requireNonNull(top, "Find pattern failed for top");
        Objects.requireNonNull(bottom, "Find pattern failed for bottom");

        LabelNode cancelNode = new LabelNode();

        InsnList insnList = new InsnList();
        insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldDisableCaveCulling));
        insnList.add(new JumpInsnNode(IFNE, cancelNode));

        main.instructions.insertBefore(top, insnList);
        main.instructions.insertBefore(bottom, cancelNode);
    }

    @Inject(name = "computeVisibility", ret = SetVisibility.class,
    description = "Add hook that adds or logic to the jump that checks if setAllVisible(true) should be called"
    )
    public void computeVisibility(MethodNode main) {
        AbstractInsnNode node = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
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
        insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldDisableCaveCulling));
        insnList.add(new JumpInsnNode(IFEQ, nextIfStatement));
        insnList.add(orLabel);

        main.instructions.insert(node, insnList);
    }
}
