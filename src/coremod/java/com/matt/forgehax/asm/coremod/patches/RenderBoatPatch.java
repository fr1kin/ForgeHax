package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.TypesHook;
import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Opcodes;
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
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.RenderBoat_doRender);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            InsnList insnList = new InsnList();

            insnList.add(new VarInsnNode(Opcodes.ALOAD, 1)); // load the boat entity
            insnList.add(new VarInsnNode(Opcodes.FLOAD, 8)); // load the boat yaw
            insnList.add(
                ASMHelper.call(
                    Opcodes.INVOKESTATIC,
                    TypesHook.Methods.ForgeHaxHooks_onRenderBoat)); // fire the event and get the value(player
            // rotationYaw) returned by the method in
            // ForgeHaxHooks
            insnList.add(new VarInsnNode(Opcodes.FSTORE, 8)); // store it in entityYaw

            main.instructions.insert(insnList); // insert code at the top of the method
            return main;
        }


    }
}
