package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.renderer.EntityRenderer;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(EntityRenderer.class)
public class EntityRendererPatch {

    @Inject(name = "hurtCameraEffect", args = {float.class},
    description = "Add hook that allows the method to be canceled"
    )
    public void hurtCameraEffect(MethodNode main) {
        AbstractInsnNode preNode = main.instructions.getFirst();
        AbstractInsnNode postNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {RETURN}, "x");

        Objects.requireNonNull(preNode, "Find pattern failed for preNode");
        Objects.requireNonNull(postNode, "Find pattern failed for postNode");

        LabelNode endJump = new LabelNode();

        InsnList insnPre = new InsnList();
        insnPre.add(new VarInsnNode(FLOAD, 1));
        insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onHurtcamEffect));
        insnPre.add(new JumpInsnNode(IFNE, endJump));

        main.instructions.insertBefore(preNode, insnPre);
        main.instructions.insertBefore(postNode, endJump);
    }
}
