package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.TypesHook;
import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import com.matt.forgehax.asm.coremod.utils.AsmPattern;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Set;

public class EntityLivingBasePatch {

    @RegisterTransformer
    public static class Travel implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.EntityLivingBase_travel);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            AsmPattern getSlipperiness = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                .ASMType(Opcodes.INVOKEINTERFACE, Methods.IBlockState_getSlipperiness)
                .build();

            final MethodInsnNode first = getSlipperiness.test(main).getFirst();

            final MethodInsnNode second = getSlipperiness.test(first.getNext()).getFirst();

            main.instructions.insert(first, getHook(0));
            main.instructions.insert(second, getHook(1));


            return main;
        }

        private InsnList getHook(int stage) {
            InsnList list = new InsnList();
            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
            list.add(new VarInsnNode(Opcodes.ALOAD, 7));
            list.add(new LdcInsnNode(stage));
            list.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onEntityBlockSlipApply));

            return list;
        }
    }
}
