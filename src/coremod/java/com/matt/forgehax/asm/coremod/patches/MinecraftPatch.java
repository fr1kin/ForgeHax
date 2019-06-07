package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.TypesHook;
import com.matt.forgehax.asm.coremod.TypesMc;
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
import java.util.Objects;
import java.util.Set;


public class MinecraftPatch {

    // Add callback before setting leftclick timer
    @RegisterTransformer
    public static class RunTick implements Transformer<MethodNode> {
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.Minecraft_runTick);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode method, ITransformerVotingContext context) {
            InsnPattern node = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                .custom(insn -> insn.getOpcode() == Opcodes.SIPUSH && ((IntInsnNode)insn).operand == 10000)
                .opcode(Opcodes.PUTFIELD)
                .build().test(method);

            Objects.requireNonNull(node, "Failed to find SIPUSH node");

            InsnList list = new InsnList();
            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
            list.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onLeftClickCounterSet));

            AbstractInsnNode first = node.getFirst();
            method.instructions.insert(first, list);
            return method;
        }
    }

    // "Add hook to set left click"
    @RegisterTransformer
    public static class SendClickBlockToController implements Transformer<MethodNode> {
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.Minecraft_sendClickBlockToController);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode method, ITransformerVotingContext context) {
            InsnList list = new InsnList();
            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
            list.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSendClickBlockToController));
            list.add(new VarInsnNode(Opcodes.ISTORE, 1));

            method.instructions.insert(list);
            return method;
        }
    }
}
