package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.TypesHook;
import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Set;

public class ActiveRenderInfoPatch {

    @RegisterTransformer
    public static class SetProjectionField implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.ActiveRenderInfo_updateRenderInfo);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode input, ITransformerVotingContext context) {
            InsnList list = new InsnList();
            list.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_setProjection));

            input.instructions.insert(list);
            return input;
        }
    }

}
