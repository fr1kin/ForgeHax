package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.TypesHook;
//import com.matt.forgehax.asm.coremod.transformer.MethodTransformer;
import com.matt.forgehax.asm.coremod.TypesMc;
import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import com.matt.forgehax.asm.coremod.utils.AsmPattern;
import com.matt.forgehax.asm.coremod.utils.InsnPattern;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Objects;
import java.util.Set;

public class NetManagerPatch implements Opcodes {

    private static final AsmPattern WRITE_AND_FLUSH = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
            .opcodes(ALOAD, GETFIELD, ALOAD)
            .<MethodInsnNode>custom(m -> m.name.equals("writeAndFlush"))
            .opcode(ASTORE)
            .build(); // ChannelFuture channelfuture = this.channel.writeAndFlush(inPacket);

    private static final AsmPattern SEND_PACKET_POST = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
            .opcodes(ALOAD, GETSTATIC)
            .<MethodInsnNode>custom(m -> m.name.equals("addListener"))
            .opcode(POP)
            .build(); // channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);

    @RegisterTransformer
    public static class DispatchPacket implements Transformer<MethodNode> {
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.NetworkManager_dispatchPacket);
        }

        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            InsnPattern prePattern = WRITE_AND_FLUSH.test(main);
            InsnPattern postPattern = SEND_PACKET_POST.test(main);

            Objects.requireNonNull(prePattern, "Find pattern failed for writeAndFlush");
            Objects.requireNonNull(postPattern, "Find pattern failed for addListener");

            LabelNode endJump = new LabelNode();

            InsnList insnPre = new InsnList();
            insnPre.add(new VarInsnNode(ALOAD, 1)); // packet
            insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSendingPacket));
            insnPre.add(new JumpInsnNode(IFNE, endJump));

            InsnList insnPost = new InsnList();
            insnPost.add(new VarInsnNode(ALOAD, 1));
            insnPost.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSentPacket));
            insnPost.add(endJump);

            main.instructions.insertBefore(prePattern.getFirst(), insnPre);
            main.instructions.insert(postPattern.getLast(), insnPost);

            return main;
        }

    }

    @RegisterTransformer
    public static class FlushHook implements Transformer<MethodNode> {
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.NetworkManager_lambda$dispatchPacket$4);
        }

        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            InsnPattern pattern = WRITE_AND_FLUSH.test(main);

            AbstractInsnNode postNode =
                    ASMHelper.findPattern(main.instructions.getFirst(), new int[] {RETURN}, "x");

            Objects.requireNonNull(pattern, "Find pattern failed for writeAndFlush");
            Objects.requireNonNull(postNode, "Find pattern failed for postNode");

            LabelNode endJump = new LabelNode();

            InsnList insnPre = new InsnList();
            insnPre.add(new VarInsnNode(ALOAD, 3));
            insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSendingPacket));
            insnPre.add(new JumpInsnNode(IFNE, endJump));

            InsnList insnPost = new InsnList();
            insnPost.add(new VarInsnNode(ALOAD, 3));
            insnPost.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSentPacket));
            insnPost.add(endJump);

            main.instructions.insertBefore(pattern.getFirst(), insnPre);
            main.instructions.insertBefore(postNode, insnPost);
            return main;
        }

    }

    @RegisterTransformer
    public static class ChannelRead0 implements Transformer<MethodNode> {
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.NetworkManager_channelRead0);
        }

        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            InsnPattern node = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                    .opcodes(ALOAD, ALOAD, GETFIELD, INVOKESTATIC) // func_197664_a(p_channelRead0_2_, this.packetListener);
                    .build().test(main);

            Objects.requireNonNull(node, "Find pattern failed for channelRead0");

            LabelNode endJump = new LabelNode();

            InsnList insnPre = new InsnList();
            insnPre.add(new VarInsnNode(ALOAD, 2));
            insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPreReceived));
            insnPre.add(new JumpInsnNode(IFNE, endJump));

            InsnList insnPost = new InsnList();
            insnPost.add(new VarInsnNode(ALOAD, 2));
            insnPost.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPostReceived));
            insnPost.add(endJump);

            main.instructions.insertBefore(node.getFirst(), insnPre);
            main.instructions.insert(node.getLast(), insnPost);
            return main;
        }


    }
}
