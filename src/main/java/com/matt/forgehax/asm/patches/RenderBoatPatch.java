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
    private class DoRender extends MethodTransformer {
        @Override
        public ASMMethod getMethod() {
            return Methods.RenderBoat_doRender;
        }

        @Inject(description = "Add hook to set boat yaw when it's rendered")
        public void inject(MethodNode main) {

            InsnList insnList = new InsnList();

            insnList.add(new VarInsnNode(ALOAD, 1)); // load the boat entity
            insnList.add(new VarInsnNode(FLOAD, 8)); // load the boat yaw
            insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onRenderBoat)); // fire the event and get the value(player rotationYaw) returned by the method in ForgeHaxHooks
            insnList.add(new VarInsnNode(FSTORE, 8)); // store it in entityYaw

            main.instructions.insert(insnList); // insert code at the top of the method

        }
    }
}