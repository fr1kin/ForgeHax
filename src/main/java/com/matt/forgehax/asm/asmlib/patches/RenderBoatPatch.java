package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.renderer.entity.RenderBoat;
import net.minecraft.entity.item.EntityBoat;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

@Transformer(RenderBoat.class)
public class RenderBoatPatch {

    @Inject(name = "doRender",
            args = {EntityBoat.class, double.class, double.class, double.class, float.class, float.class},
    description = "Add hook to set boat yaw when it's rendered"
    )
    public void doRender(MethodNode main) {
        InsnList insnList = new InsnList();

        insnList.add(new VarInsnNode(ALOAD, 1)); // load the boat entity
        insnList.add(new VarInsnNode(FLOAD, 8)); // load the boat yaw
        insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onRenderBoat)); // fire the event and get the value(player rotationYaw) returned by the method in ForgeHaxHooks
        insnList.add(new VarInsnNode(FSTORE, 8)); // store it in entityYaw

        main.instructions.insert(insnList); // insert code at the top of the method
    }
}
