package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by Babbaj on 8/9/2017.
 */
public class RenderBoatPatch extends ClassTransformer {
    public RenderBoatPatch() {
        super(Classes.RenderBoat);
    }

    @RegisterMethodTransformer
    private class doRender extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.RenderBoat_doRender;
        }

        @Inject(description = "Add hook to set boat yaw when it's rendered")
        public void inject(MethodNode main) {

            LabelNode jump = new LabelNode();

            InsnList insnList = new InsnList();
            insnList.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isNoBoatRotationActivated));
            insnList.add(new JumpInsnNode(IFEQ, jump)); // if boatfly is not enabled skip following code


            insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onRenderBoat)); // get the value(player rotationYaw) returned by the method in ForgeHaxHooks
            insnList.add(new VarInsnNode(FSTORE, 8)); // store it in entityYaw

            insnList.add(jump); //  IFEQ will jump here and skip the code above


            main.instructions.insert(insnList); // insert code at the top of the method

        }
    }
}