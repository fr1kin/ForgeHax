package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.transformer.RegisterTransformer;
import com.matt.forgehax.asm.transformer.Transformer;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.AsmPattern;
import com.matt.forgehax.asm.utils.InsnPattern;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

import static com.matt.forgehax.asm.utils.ASMHelper.MagicOpcodes.*;

public class EntityPlayerSPPatch {

    @RegisterTransformer
    public static class ApplyLivingUpdate implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<Target> targets() {
            return ASMHelper.getTargetSet(Methods.EntityPlayerSP_livingTick);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            InsnPattern node = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                .opcodes(ALOAD)
                .ASMType(INVOKEVIRTUAL, Methods.EntityPlayerSP_isHandActive) // if (this.isHandActive()
                .opcodes(IFEQ)
                .opcodes(ALOAD, INVOKEVIRTUAL, IFNE) // && !this.isPassenger()
                .build().test(main);

            final LabelNode jumpTo = node.<JumpInsnNode>getIndex(2).label;

            InsnList insnList = new InsnList();
            insnList.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isNoSlowDownActivated));
            insnList.add(new JumpInsnNode(IFNE, jumpTo));

            main.instructions.insertBefore(node.getFirst(), insnList);

            return main;
        }
    }

    @RegisterTransformer
    public static class Tick implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<Target> targets() {
            return ASMHelper.getTargetSet(Methods.EntityPlayerSP_tick);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            InsnPattern isPassengerNode = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                .opcode(ALOAD)
                .ASMType(INVOKEVIRTUAL, Methods.Entity_isPassenger)
                .opcode(IFEQ)
                .build().test(main);

            InsnPattern onUpdateWalkingPlayerNode = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                .ASMType(INVOKESPECIAL, Methods.EntityPlayerSP_onUpdateWalkingPlayer)
                .build().test(main);

            LabelNode jump = isPassengerNode.<JumpInsnNode>getIndex(2).label;

            LabelNode eventJump = ((JumpInsnNode)jump.getPrevious()).label;

            InsnList eventPreList = new InsnList();
            eventPreList.add(new VarInsnNode(ALOAD, 0)); // this
            eventPreList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onUpdateWalkingPlayerPre));
            eventPreList.add(new JumpInsnNode(IFNE, eventJump));

            InsnList postRiding = new InsnList();
            postRiding.add(new VarInsnNode(ALOAD, 0));
            postRiding.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onUpdateWalkingPlayerPost));

            InsnList postWalking = new InsnList();
            postWalking.add(new VarInsnNode(ALOAD, 0));
            postWalking.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onUpdateWalkingPlayerPost));

            main.instructions.insertBefore(isPassengerNode.getFirst(), eventPreList);
            main.instructions.insertBefore(jump.getPrevious(), postRiding);
            main.instructions.insert(onUpdateWalkingPlayerNode.getLast(), postWalking);

            return main;
        }
    }

    @RegisterTransformer
    public static class PushOutOfBlocks implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<Target> targets() {
            return ASMHelper.getTargetSet(Methods.EntityPlayerSP_pushOutOfBlocks);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {

            LabelNode jump = new LabelNode();

            InsnList list = new InsnList();
            list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPushOutOfBlocks));
            list.add(new JumpInsnNode(IFEQ, jump));
            list.add(new InsnNode(ICONST_0));
            list.add(new InsnNode(IRETURN));
            list.add(jump);

            main.instructions.insert(list);

            return main;
        }
    }

    @RegisterTransformer
    public static class RowingBoat implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<Target> targets() {
            return ASMHelper.getTargetSet(Methods.EntityPlayerSP_isRowingBoat);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            InsnList list = new InsnList();
            list.add(new InsnNode(ICONST_0));
            list.add(new InsnNode(IRETURN));

            main.instructions.insert(list);
            return main;
        }
    }
}
