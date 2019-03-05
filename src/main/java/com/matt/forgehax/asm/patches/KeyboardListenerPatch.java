package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.transformer.RegisterTransformer;
import com.matt.forgehax.asm.transformer.Transformer;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.AsmPattern;
import com.matt.forgehax.asm.utils.InsnPattern;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class KeyboardListenerPatch {

    @RegisterTransformer
    public static class OnKeyEvent implements Transformer<MethodNode> {
        @Override
        public Set<Target> targets() {
            return ASMHelper.getTargetSet(Methods.KeyboardListener_onKeyEvent);
        }

        @Nonnull
        @Override
        public MethodNode transform(MethodNode method, ITransformerVotingContext context) {
            InsnPattern branch = new AsmPattern.Builder(AsmPattern.CODE_ONLY)
                .opcodes(LLOAD, ALOAD, GETFIELD, GETFIELD, INVOKEVIRTUAL, LCMP, IFNE)
                .build()
                .test(method);

            Objects.requireNonNull(branch, "Failed to find branch");
            final LabelNode jump = branch.<JumpInsnNode>getLast().label;
            InsnList list = new InsnList();
            list.add(new VarInsnNode(ILOAD, 3)); // key
            list.add(new VarInsnNode(ILOAD, 4)); // scanCode
            list.add(new VarInsnNode(ILOAD, 5)); // action
            list.add(new VarInsnNode(ILOAD, 6)); // modifiers
            list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onKeyEvent));

            method.instructions.insertBefore(jump, list);
            return method;
        }
    }
}
