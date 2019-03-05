package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.transformer.RegisterTransformer;
import com.matt.forgehax.asm.transformer.Transformer;
import com.matt.forgehax.asm.utils.ASMHelper;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Set;

public class ActiveRenderInfoPatch {

    @RegisterTransformer
    public static class SetProjectionField implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<Target> targets() {
            return ASMHelper.getTargetSet(Methods.ActiveRenderInfo_updateRenderInfo);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode input, ITransformerVotingContext context) {
            InsnList list = new InsnList();
            list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_setProjection));

            input.instructions.insert(list);
            return input;
        }
    }

}
