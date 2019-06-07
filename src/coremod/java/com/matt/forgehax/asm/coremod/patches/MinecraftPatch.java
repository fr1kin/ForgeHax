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
import java.util.function.Predicate;

import static com.matt.forgehax.asm.coremod.utils.AsmPattern.CODE_ONLY;


public class MinecraftPatch {

    @RegisterTransformer
    public static class Init implements Transformer<MethodNode> {
        @Override
        public Set<ITransformer.Target> targets() {
            return ASMHelper.getTargetSet(Methods.Minecraft_init);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode method, ITransformerVotingContext context) {
            AsmPattern beginPattern = new AsmPattern.Builder(CODE_ONLY)
                .custom(predMethodWithName(Classes.ClientModLoader.getInternalName(), "begin"))
                .build();
            AsmPattern endPattern = new AsmPattern.Builder(CODE_ONLY)
                .custom(predMethodWithName(Classes.ClientModLoader.getInternalName(), "end"))
                .build();

            InsnPattern begin = beginPattern.test(method);
            Objects.requireNonNull(begin, "no forge modloader begin??");
            InsnPattern end = endPattern.test(method);
            Objects.requireNonNull(end, "no forge modloader end??");

            final String forgehaxMainClass = "com/matt/forgehax/ForgeHax";
            InsnList beginHook = new InsnList();
            beginHook.insert(new MethodInsnNode(INVOKESTATIC, forgehaxMainClass, "preInit", "()V", false));

            InsnList endHook = new InsnList();
            endHook.insert(new MethodInsnNode(INVOKESTATIC, forgehaxMainClass, "init", "()V", false));

            method.instructions.insert(begin.getLast(), beginHook);
            method.instructions.insert(end.getLast(), endHook);

            return method;
        }

        // TODO: put this somewhere else
        private Predicate<AbstractInsnNode> predMethodWithName(String owner, String name) {
            return insn ->
                insn instanceof MethodInsnNode &&
                    ((MethodInsnNode)insn).owner.equals(owner) &&
                    ((MethodInsnNode)insn).name.equals(name);
        }
    }


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
            InsnPattern node = new AsmPattern.Builder(CODE_ONLY)
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
