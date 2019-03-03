package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.events.GetCollisionShapeEvent;
import com.matt.forgehax.asm.transformer.Transformer;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMClass;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Set;

public class BlockPatch {

    public static class GetCollisionShape implements Transformer<MethodNode> {
        @Override
        public Set<Target> targets() {
            return ASMHelper.getTargetSet(Methods.Block_getCollisionShape);
        }

        @Override
        public MethodNode transform(MethodNode method, ITransformerVotingContext context) {
            AbstractInsnNode node = method.instructions.getFirst();

            LabelNode jump = new LabelNode();
            final int eventIdx = ASMHelper.addNewLocalVariable(method, "forgehax_event", Type.getDescriptor(GetCollisionShapeEvent.class));

            InsnList list = new InsnList();
                InsnList eventArgs = new InsnList();
                eventArgs.add(new VarInsnNode(ALOAD, 0)); // this (Block)
                eventArgs.add(new VarInsnNode(ALOAD, 1)); // state
                eventArgs.add(new VarInsnNode(ALOAD, 2)); // worldIn (IBlockReader)
                eventArgs.add(new VarInsnNode(ALOAD, 3)); // pos
            list.add(ASMHelper.newInstance(Type.getInternalName(GetCollisionShapeEvent.class), new ASMClass[]{Classes.Block, Classes.IBlockState, Classes.IBlockReader, Classes.BlockPos}, eventArgs));
            list.add(new VarInsnNode(ASTORE, eventIdx));
            list.add(new VarInsnNode(ALOAD, eventIdx));
            list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_fireEvent_b));
            list.add(new JumpInsnNode(IFNE, jump));
            list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(GetCollisionShapeEvent.class), "getReturnShape", "()" + Classes.VoxelShape.getDescriptor()));
            list.add(new InsnNode(ARETURN));
            list.add(jump);


            return method;
        }
    }
}
