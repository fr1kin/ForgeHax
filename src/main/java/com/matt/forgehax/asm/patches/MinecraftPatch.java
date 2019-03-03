package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.TypesMc;
import com.matt.forgehax.asm.transformer.Transformer;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.AsmPattern;
import com.matt.forgehax.asm.utils.InsnPattern;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;


public class MinecraftPatch {

    // Add callback before setting leftclick timer
    public static class RunTick implements Transformer<MethodNode> {
        @Override
        public Set<Target> targets() {
            return ASMHelper.getTargetSet(Methods.Minecraft_runTick);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode method, ITransformerVotingContext context) {
            InsnPattern node = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                .custom(insn -> insn.getOpcode() == SIPUSH && ((IntInsnNode)insn).operand == 10000)
                .opcode(PUTFIELD)
                .build().test(method);

            Objects.requireNonNull(node, "Failed to find SIPUSH node");

            InsnList list = new InsnList();
            list.add(new VarInsnNode(ALOAD, 0));
            list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onLeftClickCounterSet));

            AbstractInsnNode first = node.getFirst();
            method.instructions.insert(first, list);
            return method;
        }
    }

    // "Add hook to set left click"
    public static class SendClickBlockToController implements Transformer<MethodNode> {
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.Minecraft_sendClickBlockToController);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode method, ITransformerVotingContext context) {
            InsnList list = new InsnList();
            list.add(new VarInsnNode(ALOAD, 0));
            list.add(new VarInsnNode(ILOAD, 1));
            list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSendClickBlockToController));
            list.add(new VarInsnNode(ISTORE, 1));

            method.instructions.insert(list);
            return method;
        }
    }
}
