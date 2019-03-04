package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.transformer.RegisterTransformer;
import com.matt.forgehax.asm.transformer.Transformer;
import com.matt.forgehax.asm.utils.ASMHelper;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import javax.annotation.Nonnull;
import java.util.Set;

public class RenderBoatPatch {

    @RegisterTransformer
    public static class DoRender implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<Target> targets() {
            return ASMHelper.getTargetSet(Methods.RenderBoat_doRender);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            InsnList insnList = new InsnList();

            insnList.add(new VarInsnNode(ALOAD, 1)); // load the boat entity
            insnList.add(new VarInsnNode(FLOAD, 8)); // load the boat yaw
            insnList.add(
                ASMHelper.call(
                    INVOKESTATIC,
                    TypesHook.Methods.ForgeHaxHooks_onRenderBoat)); // fire the event and get the value(player
            // rotationYaw) returned by the method in
            // ForgeHaxHooks
            insnList.add(new VarInsnNode(FSTORE, 8)); // store it in entityYaw

            main.instructions.insert(insnList); // insert code at the top of the method
            return main;
        }


    }
}
