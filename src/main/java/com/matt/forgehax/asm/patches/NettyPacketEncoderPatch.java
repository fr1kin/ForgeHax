package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.*;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class NettyPacketEncoderPatch extends ClassTransformer {
    public NettyPacketEncoderPatch() {
        super(Classes.NettyPacketEncoder);
    }

    @RegisterMethodTransformer
    private class PacketEncode extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.NettyPacketEncoder_encode;
        }

        @Inject(description = "Add hook to listen for and override packet encoding")
        public void inject(MethodNode main) {
            AbstractInsnNode node = ASMHelper.findPattern(main.instructions.getFirst(), new int[]{
                    ALOAD, ALOAD, INVOKEINTERFACE
            }, "xxx"); // p_encode_2_.writePacketData(packetbuffer);

            Objects.requireNonNull(node, "Find pattern failed for node");

            LabelNode jump = new LabelNode();

            InsnList list = new InsnList();
            list.add(new VarInsnNode(ALOAD, 2)); // packet
            list.add(new VarInsnNode(ALOAD, 6)); // packetbuffer
            list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgehaxHooks_onPacketEncode));
            list.add(new JumpInsnNode(IFNE, jump));
            main.instructions.insertBefore(node, list);

            AbstractInsnNode post = node.getNext().getNext(); // invokeinterface
            main.instructions.insert(post, jump);

        }
    }

}
