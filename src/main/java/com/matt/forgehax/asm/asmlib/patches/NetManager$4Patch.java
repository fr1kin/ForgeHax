package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.TypesMc;
import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(target = "net.minecraft.network.NetworkManager$4")
public class NetManager$4Patch {

    @Inject(name = "run",
    description = "Add a pre and post hook that allows the method to be disabled"
    )
    public void run(MethodNode main) {
        AbstractInsnNode preNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                ALOAD, GETFIELD, ALOAD, GETFIELD, IF_ACMPEQ
        }, "xxxxx");

        AbstractInsnNode postNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {RETURN}, "x");

        Objects.requireNonNull(preNode, "Find pattern failed for preNode");
        Objects.requireNonNull(postNode, "Find pattern failed for postNode");

        LabelNode endJump = new LabelNode();

        InsnList insnPre = new InsnList();
        insnPre.add(new VarInsnNode(ALOAD, 0));
        insnPre.add(ASMHelper.call(GETFIELD, TypesMc.Fields.NetworkManager$4_val$inPacket));
        insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSendingPacket));
        insnPre.add(new JumpInsnNode(IFNE, endJump));

        InsnList insnPost = new InsnList();
        insnPost.add(new VarInsnNode(ALOAD, 0));
        insnPost.add(ASMHelper.call(GETFIELD, TypesMc.Fields.NetworkManager$4_val$inPacket));
        insnPost.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSentPacket));
        insnPost.add(endJump);

        main.instructions.insertBefore(preNode, insnPre);
        main.instructions.insertBefore(postNode, insnPost);
    }
}
