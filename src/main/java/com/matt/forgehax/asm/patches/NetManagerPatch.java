package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmHelper;
import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.transforming.ClassTransformer;
import com.matt.forgehax.asm.helper.transforming.Inject;
import com.matt.forgehax.asm.helper.transforming.MethodTransformer;
import com.matt.forgehax.asm.helper.transforming.RegisterMethodTransformer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class NetManagerPatch extends ClassTransformer {
    public final AsmMethod DISPATCH_PACKET = new AsmMethod()
            .setName("dispatchPacket")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.PACKET, GenericFutureListener[].class)
            .setReturnType(void.class)
            .setHooks(NAMES.ON_SENT_PACKET, NAMES.ON_SENDING_PACKET);

    public final AsmMethod CHANNEL_READ0 = new AsmMethod()
            .setName("channelRead0")
            .setObfuscatedName("a")
            .setArgumentTypes(ChannelHandlerContext.class, NAMES.PACKET)
            .setReturnType(void.class)
            .setHooks(NAMES.ON_POST_RECEIVED, NAMES.ON_PRE_RECEIVED);

    public NetManagerPatch() {
        super("net/minecraft/network/NetworkManager");
    }

    @RegisterMethodTransformer
    private class DispatchPacket extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return DISPATCH_PACKET;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode preNode = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ALOAD, ALOAD, IF_ACMPEQ, ALOAD, INSTANCEOF, IFNE,
                    0x00, 0x00,
                    ALOAD, ALOAD, INVOKEVIRTUAL
            }, "xxxxxx??xxx");
            AbstractInsnNode postNode = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
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
            insnPre.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_SENDING_PACKET.getParentClass().getRuntimeName(),
                    NAMES.ON_SENDING_PACKET.getRuntimeName(),
                    NAMES.ON_SENDING_PACKET.getDescriptor(),
                    false
            ));
            insnPre.add(new JumpInsnNode(IFNE, endJump));

            InsnList insnPost = new InsnList();
            insnPost.add(new VarInsnNode(ALOAD, 1));
            insnPost.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_SENT_PACKET.getParentClass().getRuntimeName(),
                    NAMES.ON_SENT_PACKET.getRuntimeName(),
                    NAMES.ON_SENT_PACKET.getDescriptor(),
                    false
            ));
            insnPost.add(endJump);

            main.instructions.insertBefore(preNode, insnPre);
            main.instructions.insert(postNode, insnPost);
        }
    }

    @RegisterMethodTransformer
    private class ChannelRead0 extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return CHANNEL_READ0;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode preNode = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ALOAD, ALOAD, GETFIELD, INVOKEINTERFACE
            }, "xxxx");
            AbstractInsnNode postNode = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    INVOKEINTERFACE,
                    0x00, 0x00,
                    GOTO,
            }, "x??x");

            Objects.requireNonNull(preNode, "Find pattern failed for preNode");
            Objects.requireNonNull(postNode, "Find pattern failed for postNode");

            LabelNode endJump = new LabelNode();

            InsnList insnPre = new InsnList();
            insnPre.add(new VarInsnNode(ALOAD, 2));
            insnPre.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_PRE_RECEIVED.getParentClass().getRuntimeName(),
                    NAMES.ON_PRE_RECEIVED.getRuntimeName(),
                    NAMES.ON_PRE_RECEIVED.getDescriptor(),
                    false
            ));
            insnPre.add(new JumpInsnNode(IFNE, endJump));

            InsnList insnPost = new InsnList();
            insnPost.add(new VarInsnNode(ALOAD, 2));
            insnPost.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_POST_RECEIVED.getParentClass().getRuntimeName(),
                    NAMES.ON_POST_RECEIVED.getRuntimeName(),
                    NAMES.ON_POST_RECEIVED.getDescriptor(),
                    false
            ));
            insnPost.add(endJump);

            main.instructions.insertBefore(preNode, insnPre);
            main.instructions.insert(postNode, insnPost);
        }
    }
}
