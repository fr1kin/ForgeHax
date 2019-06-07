package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.TypesHook;
import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.events.GetCollisionShapeEvent;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import com.matt.forgehax.asm.coremod.utils.asmtype.ASMClass;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Set;

public class BlockPatch {

    @RegisterTransformer
    public static class GetCollisionShape implements Transformer<MethodNode> {
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.Block_getCollisionShape);
        }

        @Override
        public MethodNode transform(MethodNode method, ITransformerVotingContext context) {
            AbstractInsnNode node = method.instructions.getFirst();

            LabelNode jump = new LabelNode();
            final int eventIdx = ASMHelper.addNewLocalVariable(method, "forgehax_event", Type.getDescriptor(GetCollisionShapeEvent.class));

            InsnList list = new InsnList();
                InsnList eventArgs = new InsnList();
                eventArgs.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this (Block)
                eventArgs.add(new VarInsnNode(Opcodes.ALOAD, 1)); // state
                eventArgs.add(new VarInsnNode(Opcodes.ALOAD, 2)); // worldIn (IBlockReader)
                eventArgs.add(new VarInsnNode(Opcodes.ALOAD, 3)); // pos
            list.add(ASMHelper.newInstance(Type.getInternalName(GetCollisionShapeEvent.class), new ASMClass[]{Classes.Block, Classes.IBlockState, Classes.IBlockReader, Classes.BlockPos}, eventArgs));
            list.add(new VarInsnNode(Opcodes.ASTORE, eventIdx));
            list.add(new VarInsnNode(Opcodes.ALOAD, eventIdx));
            list.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_fireEvent_b));
            list.add(new JumpInsnNode(Opcodes.IFNE, jump));
            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(GetCollisionShapeEvent.class), "getReturnShape", "()" + Classes.VoxelShape.getDescriptor()));
            list.add(new InsnNode(Opcodes.ARETURN));
            list.add(jump);


            return method;
        }
    }
}
