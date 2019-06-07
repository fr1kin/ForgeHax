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

public class BufferBuilderPatch {

    @RegisterTransformer
    public static class PutColorMultiplier implements Transformer<MethodNode> {
        @Nonnull
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.BufferBuilder_putColorMultiplier);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
            AbstractInsnNode preNode =
                ASMHelper.findPattern(
                    main.instructions.getFirst(),
                    new int[] {
                        Opcodes.INVOKESTATIC,
                        Opcodes.GETSTATIC,
                        Opcodes.IF_ACMPNE,
                        0x00,
                        0x00,
                        Opcodes.ILOAD,
                        Opcodes.SIPUSH,
                        Opcodes.IAND,
                        Opcodes.I2F,
                        Opcodes.FLOAD,
                        Opcodes.FMUL,
                        Opcodes.F2I,
                        Opcodes.ISTORE
                    },
                    "xxx??xxxxxxxx");
            AbstractInsnNode postNode =
                ASMHelper.findPattern(
                    main.instructions.getFirst(),
                    new int[] {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.ILOAD, Opcodes.INVOKEVIRTUAL, Opcodes.POP},
                    "xxxxxx");

            Objects.requireNonNull(preNode, "Find pattern failed for preNode");
            Objects.requireNonNull(postNode, "Find pattern failed for postNode");

            LabelNode endJump = new LabelNode();

            InsnList insnPre = new InsnList();
            insnPre.add(new InsnNode(Opcodes.ICONST_1));
            insnPre.add(new IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN));
            insnPre.add(new InsnNode(Opcodes.DUP));
            insnPre.add(new InsnNode(Opcodes.ICONST_0));
            insnPre.add(new InsnNode(Opcodes.ICONST_0));
            insnPre.add(new InsnNode(Opcodes.BASTORE));
            insnPre.add(new VarInsnNode(Opcodes.ASTORE, 10));
            insnPre.add(new VarInsnNode(Opcodes.FLOAD, 1));
            insnPre.add(new VarInsnNode(Opcodes.FLOAD, 2));
            insnPre.add(new VarInsnNode(Opcodes.FLOAD, 3));
            insnPre.add(new VarInsnNode(Opcodes.ILOAD, 6));
            insnPre.add(new VarInsnNode(Opcodes.ALOAD, 10));
            insnPre.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPutColorMultiplier));
            insnPre.add(new VarInsnNode(Opcodes.ISTORE, 6));
            insnPre.add(new VarInsnNode(Opcodes.ALOAD, 10));
            insnPre.add(new InsnNode(Opcodes.ICONST_0));
            insnPre.add(new InsnNode(Opcodes.BALOAD));
            insnPre.add(new JumpInsnNode(Opcodes.IFNE, endJump));

            main.instructions.insertBefore(preNode, insnPre);
            main.instructions.insertBefore(postNode, endJump);

            return main;
        }
    }
}
