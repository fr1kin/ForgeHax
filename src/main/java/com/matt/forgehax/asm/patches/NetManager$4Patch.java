package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.*;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class NetManager$4Patch extends ClassTransformer {
    public final AsmMethod RUN = new AsmMethod()
            .setName("run")
            .setObfuscatedName("run")
            .setArgumentTypes()
            .setReturnType(void.class)
            .setHooks(NAMES.ON_SENDING_PACKET, NAMES.ON_SENT_PACKET);

    public NetManager$4Patch() {
        registerHook(RUN);
    }

    @Override
    public boolean onTransformMethod(MethodNode method) {
        if(method.name.equals(RUN.getRuntimeName()) &&
                method.desc.equals(RUN.getDescriptor())) {
            updatePatchedMethods(patchRun(method));
            return true;
        } else return false;
    }

    private final int[] patternPreDispatch = new int[] {
            ALOAD, GETFIELD, ALOAD, GETFIELD, IF_ACMPEQ
    };

    private final int[] patternPostDispatch = new int[] {
            RETURN
    };

    private boolean patchRun(MethodNode method) {
        AbstractInsnNode preNode = null, postNode = null;
        try {
            preNode = AsmHelper.findPattern(method.instructions.getFirst(),
                    patternPreDispatch, "xxxxx");
        } catch (Exception e) {
            log("dispatchPacket", "preNode error: %s\n", e.getMessage());
        }
        try {
            postNode = AsmHelper.findPattern(method.instructions.getFirst(),
                    patternPostDispatch, "x");
        } catch (Exception e) {
            log("dispatchPacket", "postNode error: %s\n", e.getMessage());
        }
        if(preNode != null && postNode != null) {
            LabelNode endJump = new LabelNode();

            InsnList insnPre = new InsnList();
            insnPre.add(new VarInsnNode(ALOAD, 0));
            insnPre.add(new FieldInsnNode(GETFIELD,
                    NAMES.NETMANAGER$4__val$inPacket.getParentClass().getRuntimeName(),
                    NAMES.NETMANAGER$4__val$inPacket.getRuntimeName(),
                    NAMES.NETMANAGER$4__val$inPacket.getTypeDescriptor()
            ));
            insnPre.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_SENDING_PACKET.getParentClass().getRuntimeName(),
                    NAMES.ON_SENDING_PACKET.getRuntimeName(),
                    NAMES.ON_SENDING_PACKET.getDescriptor(),
                    false
            ));
            insnPre.add(new JumpInsnNode(IFNE, endJump));

            InsnList insnPost = new InsnList();
            insnPost.add(new VarInsnNode(ALOAD, 0));
            insnPost.add(new FieldInsnNode(GETFIELD,
                    NAMES.NETMANAGER$4__val$inPacket.getParentClass().getRuntimeName(),
                    NAMES.NETMANAGER$4__val$inPacket.getRuntimeName(),
                    NAMES.NETMANAGER$4__val$inPacket.getTypeDescriptor()
            ));
            insnPost.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_SENT_PACKET.getParentClass().getRuntimeName(),
                    NAMES.ON_SENT_PACKET.getRuntimeName(),
                    NAMES.ON_SENT_PACKET.getDescriptor(),
                    false
            ));
            insnPost.add(endJump);

            method.instructions.insertBefore(preNode, insnPre);
            method.instructions.insertBefore(postNode, insnPost);
            return true;
        } else return false;
    }
}
