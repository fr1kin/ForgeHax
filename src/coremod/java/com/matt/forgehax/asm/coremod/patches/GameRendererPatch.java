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
import java.util.Objects;
import java.util.Set;

public class GameRendererPatch {

    @RegisterTransformer
    public static class HurtCameraEffect implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.GameRenderer_hurtCameraEffect);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            AbstractInsnNode preNode = main.instructions.getFirst();
            AbstractInsnNode postNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {Opcodes.RETURN}, "x");

            Objects.requireNonNull(preNode, "Find pattern failed for preNode");
            Objects.requireNonNull(postNode, "Find pattern failed for postNode");

            LabelNode endJump = new LabelNode();

            InsnList insnPre = new InsnList();
            insnPre.add(new VarInsnNode(Opcodes.FLOAD, 1));
            insnPre.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onHurtcamEffect));
            insnPre.add(new JumpInsnNode(Opcodes.IFNE, endJump));

            main.instructions.insertBefore(preNode, insnPre);
            main.instructions.insertBefore(postNode, endJump);
            return main;
        }
    }
}
