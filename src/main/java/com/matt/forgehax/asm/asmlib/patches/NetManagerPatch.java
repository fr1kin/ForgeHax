package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(NetworkManager.class)
public class NetManagerPatch {

    @Inject(name = "dispatchPacket", args = {Packet.class, GenericFutureListener[].class},
    description = "Add pre and post hooks that allow method to be disabled"
    )
    public void dispatchPacket(MethodNode main) {
        AbstractInsnNode preNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                ALOAD, ALOAD, IF_ACMPEQ, ALOAD, INSTANCEOF, IFNE,
                0x00, 0x00,
                ALOAD, ALOAD, INVOKEVIRTUAL
        }, "xxxxxx??xxx");
        AbstractInsnNode postNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                POP,
                0x00, 0x00,
                GOTO,
                0x00, 0x00, 0x00,
                ALOAD, GETFIELD, INVOKEINTERFACE, NEW, DUP
        }, "x??x???xxxxx");

        Objects.requireNonNull(preNode, "Find pattern failed for preNode");
        Objects.requireNonNull(postNode, "Find pattern failed for postNode");

        LabelNode endJump = new LabelNode();

        InsnList insnPre = new InsnList();
        insnPre.add(new VarInsnNode(ALOAD, 1));
        insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSendingPacket));
        insnPre.add(new JumpInsnNode(IFNE, endJump));

        InsnList insnPost = new InsnList();
        insnPost.add(new VarInsnNode(ALOAD, 1));
        insnPost.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSentPacket));
        insnPost.add(endJump);

        main.instructions.insertBefore(preNode, insnPre);
        main.instructions.insert(postNode, insnPost);
    }

    // manually set name because this isn't a vanilla method
    // real name is channelRead0
    @Inject(name = "a", args = {ChannelHandlerContext.class, Packet.class},
    description = "Add pre and post hook that allows the method to be disabled"
    )
    public void channelRead0(MethodNode main) {
        AbstractInsnNode preNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                ALOAD, ALOAD, GETFIELD, INVOKEINTERFACE
        }, "xxxx");
        AbstractInsnNode postNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                INVOKEINTERFACE,
                0x00, 0x00,
                GOTO,
        }, "x??x");

        Objects.requireNonNull(preNode, "Find pattern failed for preNode");
        Objects.requireNonNull(postNode, "Find pattern failed for postNode");

        LabelNode endJump = new LabelNode();

        InsnList insnPre = new InsnList();
        insnPre.add(new VarInsnNode(ALOAD, 2));
        insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPreReceived));
        insnPre.add(new JumpInsnNode(IFNE, endJump));

        InsnList insnPost = new InsnList();
        insnPost.add(new VarInsnNode(ALOAD, 2));
        insnPost.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPostReceived));
        insnPost.add(endJump);

        main.instructions.insertBefore(preNode, insnPre);
        main.instructions.insert(postNode, insnPost);
    }
}
