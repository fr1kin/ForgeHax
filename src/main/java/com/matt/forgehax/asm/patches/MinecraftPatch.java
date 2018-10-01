package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMClass;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.TypeHost;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class MinecraftPatch extends ClassTransformer {
    public MinecraftPatch() {
        super(Classes.Minecraft);
    }

    @RegisterMethodTransformer
    public class SetIngameFocus extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.Minecraft_setIngameFocus;
        }

        @Inject(description = "Add callback before setting leftclick timer")
        public void inject(MethodNode method) {
            AbstractInsnNode node = ASMHelper.findPattern(method.instructions.getFirst(), new int[] {SIPUSH, PUTFIELD, 0, 0, 0, RETURN}, "xx???x");
            Objects.requireNonNull(node, "Failed to find SIPUSH node");

            InsnList list = new InsnList();
            list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onLeftClickCounterSet));

            method.instructions.insert(node, list);
        }
    }

    @RegisterMethodTransformer
    public class RunTick extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.Minecraft_runTick;
        }

        @Inject(description = "Add callback before setting leftclick timer")
        public void inject(MethodNode method) {
            AbstractInsnNode node = ASMHelper.findPattern(method.instructions.getFirst(), new int[] {SIPUSH, PUTFIELD, 0, 0, 0, ALOAD, GETFIELD, IFNULL, 0, 0, ALOAD, GETFIELD, INVOKEVIRTUAL}, "xx???xxx??xxx");
            Objects.requireNonNull(node, "Failed to find SIPUSH node");

            InsnList list = new InsnList();
            list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onLeftClickCounterSet));

            method.instructions.insert(node, list);
        }

        @Inject(description = "Add callback for userinput hook")
        public void inject2(MethodNode method) {
            AbstractInsnNode node = ASMHelper.findPattern(method.instructions.getFirst(), new int[] {IFNULL, ALOAD, GETFIELD, GETFIELD, IFEQ, 0, 0, 0, ALOAD, GETFIELD, LDC}, "xxxxx???xxx");
            Objects.requireNonNull(node, "Failed to find IFNULL");

            JumpInsnNode jmp = (JumpInsnNode)node;
            LabelNode to = jmp.label;

            InsnList list = new InsnList();
            list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_isUserInputAllowed));
            list.add(new JumpInsnNode(IFNE, to));

            method.instructions.insert(node, list);
        }
    }

    @RegisterMethodTransformer
    public class SendClickBlockToController extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.Minecraft_sendClickBlockToController;
        }

        @Inject(description = "Add hook to set left click")
        public void inject(MethodNode method) {
            InsnList list = new InsnList();
            list.add(new VarInsnNode(ILOAD, 1));
            list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSendClickBlockToController));
            list.add(new VarInsnNode(ISTORE, 1));

            method.instructions.insert(list);
        }
    }
}
