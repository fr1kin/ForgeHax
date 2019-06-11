package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.TypesHook;
import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import com.matt.forgehax.asm.coremod.utils.AsmPattern;
import com.matt.forgehax.asm.coremod.utils.InsnPattern;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Set;


public class ClientPlayerEntity {

    @RegisterTransformer
    public static class ApplyLivingUpdate implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.ClientPlayerEntity_livingTick);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            InsnPattern node = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                .opcodes(Opcodes.ALOAD)
                .ASMType(Opcodes.INVOKEVIRTUAL, Methods.ClientPlayerEntity_isHandActive) // if (this.isHandActive()
                .opcodes(Opcodes.IFEQ)
                .opcodes(Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL, Opcodes.IFNE) // && !this.isPassenger()
                .build().test(main);

            final LabelNode jumpTo = node.<JumpInsnNode>getIndex(2).label;

            InsnList insnList = new InsnList();
            insnList.add(ASMHelper.call(Opcodes.GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isNoSlowDownActivated));
            insnList.add(new JumpInsnNode(Opcodes.IFNE, jumpTo));

            main.instructions.insertBefore(node.getFirst(), insnList);

            return main;
        }
    }

    @RegisterTransformer
    public static class Tick implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.ClientPlayerEntity_tick);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            InsnPattern isPassengerNode = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                .opcode(Opcodes.ALOAD)
                .ASMType(Opcodes.INVOKEVIRTUAL, Methods.Entity_isPassenger)
                .opcode(Opcodes.IFEQ)
                .build().test(main);

            InsnPattern onUpdateWalkingPlayerNode = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                .ASMType(Opcodes.INVOKESPECIAL, Methods.ClientPlayerEntity_onUpdateWalkingPlayer)
                .build().test(main);

            LabelNode jump = isPassengerNode.<JumpInsnNode>getIndex(2).label;

            LabelNode eventJump = ((JumpInsnNode)jump.getPrevious()).label;

            InsnList eventPreList = new InsnList();
            eventPreList.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
            eventPreList.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onUpdateWalkingPlayerPre));
            eventPreList.add(new JumpInsnNode(Opcodes.IFNE, eventJump));

            InsnList postRiding = new InsnList();
            postRiding.add(new VarInsnNode(Opcodes.ALOAD, 0));
            postRiding.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onUpdateWalkingPlayerPost));

            InsnList postWalking = new InsnList();
            postWalking.add(new VarInsnNode(Opcodes.ALOAD, 0));
            postWalking.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onUpdateWalkingPlayerPost));

            main.instructions.insertBefore(isPassengerNode.getFirst(), eventPreList);
            main.instructions.insertBefore(jump.getPrevious(), postRiding);
            main.instructions.insert(onUpdateWalkingPlayerNode.getLast(), postWalking);

            return main;
        }
    }



    @RegisterTransformer
    public static class RowingBoat implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.ClientPlayerEntity_isRowingBoat);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            InsnList list = new InsnList();
            list.add(new InsnNode(Opcodes.ICONST_0));
            list.add(new InsnNode(Opcodes.IRETURN));

            main.instructions.insert(list);
            return main;
        }
    }
}
