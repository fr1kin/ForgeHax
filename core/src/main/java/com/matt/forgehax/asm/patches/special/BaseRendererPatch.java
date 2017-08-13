package com.matt.forgehax.asm.patches.special;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.TypesSpecial;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created on 5/2/2017 by fr1kin
 */
public class BaseRendererPatch extends ClassTransformer {
    public BaseRendererPatch() {
        super(TypesSpecial.Classes.BaseRenderer);
    }

    @RegisterMethodTransformer
    private class SetStratumColors extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return TypesSpecial.Methods.BaseRenderer_setStratumColors;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode start = main.instructions.getFirst();
            AbstractInsnNode end = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                    RETURN
            }, "x");

            Objects.requireNonNull(end, "Find pattern failed for end");

            LabelNode endJump = new LabelNode();

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 0));
            insnList.add(new VarInsnNode(ALOAD, 1));
            insnList.add(new VarInsnNode(ILOAD, 2));
            insnList.add(new VarInsnNode(ALOAD, 3));
            insnList.add(new VarInsnNode(ILOAD, 4));
            insnList.add(new VarInsnNode(ILOAD, 5));
            insnList.add(new VarInsnNode(ILOAD, 6));
            insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onJournyMapSetStratumColor));
            insnList.add(new JumpInsnNode(IFNE, endJump));

            main.instructions.insertBefore(end, endJump);
            main.instructions.insertBefore(start, insnList);
        }
    }
}
