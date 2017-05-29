package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
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
 * Created on 11/13/2016 by fr1kin
 */
public class EntityPlayerSPPatch extends ClassTransformer {
    public EntityPlayerSPPatch() {
        super(Classes.EntityPlayerSP);
    }

    @RegisterMethodTransformer
    private class ApplyLivingUpdate extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.EntityPlayerSP_onLivingUpdate;
        }

        @Inject(description = "Add hook to disable the use slowdown effect")
        public void inject(MethodNode main) {
            AbstractInsnNode applySlowdownSpeedNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                    IFNE,
                    0x00, 0x00,
                    ALOAD, GETFIELD, DUP, GETFIELD, LDC, FMUL, PUTFIELD
            }, "x??xxxxxxx");

            Objects.requireNonNull(applySlowdownSpeedNode, "Find pattern failed for applySlowdownSpeedNode");

            // get label it jumps to
            LabelNode jumpTo = ((JumpInsnNode) applySlowdownSpeedNode).label;

            InsnList insnList = new InsnList();
            insnList.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isNoSlowDownActivated));
            insnList.add(new JumpInsnNode(IFNE, jumpTo));

            main.instructions.insert(applySlowdownSpeedNode, insnList);
        }
    }
}
