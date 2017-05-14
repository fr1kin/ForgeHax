package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.*;
import com.matt.forgehax.asm.helper.transforming.ClassTransformer;
import com.matt.forgehax.asm.helper.transforming.Inject;
import com.matt.forgehax.asm.helper.transforming.MethodTransformer;
import com.matt.forgehax.asm.helper.transforming.RegisterMethodTransformer;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class NetManager$4Patch extends ClassTransformer {
    public final AsmMethod RUN = new AsmMethod()
            .setName("run")
            .setObfuscatedName("run")
            .setArgumentTypes()
            .setReturnType(void.class)
            .setHooks(NAMES.ON_SENDING_PACKET, NAMES.ON_SENT_PACKET);

    public NetManager$4Patch() {
        super("net/minecraft/network/NetworkManager$4");
    }

    @RegisterMethodTransformer
    private class Run extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return RUN;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode preNode = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ALOAD, GETFIELD, ALOAD, GETFIELD, IF_ACMPEQ
            }, "xxxxx");

            AbstractInsnNode postNode = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {RETURN}, "x");

            Objects.requireNonNull(preNode, "Find pattern failed for preNode");
            Objects.requireNonNull(postNode, "Find pattern failed for postNode");

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

            main.instructions.insertBefore(preNode, insnPre);
            main.instructions.insertBefore(postNode, insnPost);
        }
    }
}
