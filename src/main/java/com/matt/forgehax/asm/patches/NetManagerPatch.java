package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmHelper;
import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.ClassTransformer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

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
        registerHook(DISPATCH_PACKET);
        registerHook(CHANNEL_READ0);
    }

    @Override
    public boolean onTransformMethod(MethodNode method) {
        if(method.name.equals(DISPATCH_PACKET.getRuntimeName()) &&
                method.desc.equals(DISPATCH_PACKET.getDescriptor())) {
            updatePatchedMethods(patchDispatchPacket(method));
            return true;
        } else if(method.name.equals(CHANNEL_READ0.getRuntimeName()) &&
                method.desc.equals(CHANNEL_READ0.getDescriptor())) {
            updatePatchedMethods(patchChannelRead0(method));
            return true;
        }
        return false;
    }

    private final int[] patternPreDispatch = new int[] {
            ALOAD, ALOAD, IF_ACMPEQ, ALOAD, INSTANCEOF, IFNE,
            0x00, 0x00,
            ALOAD, ALOAD, INVOKEVIRTUAL
    };

    private final int[] patternPostDispatch = new int[] {
            POP,
            0x00, 0x00,
            GOTO,
            0x00, 0x00, 0x00,
            ALOAD, GETFIELD, INVOKEINTERFACE, NEW, DUP
    };

    private boolean patchDispatchPacket(MethodNode method) {
        AbstractInsnNode preNode = null, postNode = null;
        try {
            preNode = AsmHelper.findPattern(method.instructions.getFirst(),
                    patternPreDispatch, "xxxxxx??xxx");
        } catch (Exception e) {
            log("dispatchPacket", "preNode error: %s\n", e.getMessage());
        }
        try {
            postNode = AsmHelper.findPattern(method.instructions.getFirst(),
                    patternPostDispatch, "x??x???xxxxx");
        } catch (Exception e) {
            log("dispatchPacket", "postNode error: %s\n", e.getMessage());
        }
        if(preNode != null && postNode != null) {
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

            method.instructions.insertBefore(preNode, insnPre);
            method.instructions.insert(postNode, insnPost);
            return true;
        } else {
            return false;
        }
    }

    private final int[] patternPreSend = new int[] {
            ALOAD, ALOAD, GETFIELD, INVOKEINTERFACE
    };

    private final int[] patternPostSend = new int[] {
            INVOKEINTERFACE,
            0x00, 0x00,
            GOTO,
    };

    private boolean patchChannelRead0(MethodNode method) {
        AbstractInsnNode preNode = null, postNode = null;
        try {
            preNode = AsmHelper.findPattern(method.instructions.getFirst(),
                    patternPreSend, "xxxx");
        } catch (Exception e) {
            log("channelRead0", "preNode error: %s\n", e.getMessage());
        }
        try {
            postNode = AsmHelper.findPattern(method.instructions.getFirst(),
                    patternPostSend, "x??x");
        } catch (Exception e) {
            log("channelRead0", "postNode error: %s\n", e.getMessage());
        }
        if(preNode != null && postNode != null) {
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

            method.instructions.insertBefore(preNode, insnPre);
            method.instructions.insert(postNode, insnPost);
            return true;
        } else {
            return false;
        }
    }
}
