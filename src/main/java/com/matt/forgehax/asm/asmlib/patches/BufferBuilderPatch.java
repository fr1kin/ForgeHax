package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.renderer.BufferBuilder;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(BufferBuilder.class)
public class BufferBuilderPatch {

    @Inject(name = "putColorMultiplier", args = {float.class, float.class, float.class, int.class},
    description = "Add hook that allows method to be overwritten"
    )
    public void putColorMultiplier(MethodNode main) {
        AbstractInsnNode preNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                INVOKESTATIC, GETSTATIC, IF_ACMPNE,
                0x00, 0x00,
                ILOAD, SIPUSH, IAND, I2F, FLOAD, FMUL, F2I, ISTORE
        }, "xxx??xxxxxxxx");
        AbstractInsnNode postNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                ALOAD, GETFIELD, ILOAD, ILOAD, INVOKEVIRTUAL, POP
        }, "xxxxxx");

        Objects.requireNonNull(preNode, "Find pattern failed for preNode");
        Objects.requireNonNull(postNode, "Find pattern failed for postNode");

        LabelNode endJump = new LabelNode();

        InsnList insnPre = new InsnList();
        insnPre.add(new InsnNode(ICONST_1));
        insnPre.add(new IntInsnNode(NEWARRAY, T_BOOLEAN));
        insnPre.add(new InsnNode(DUP));
        insnPre.add(new InsnNode(ICONST_0));
        insnPre.add(new InsnNode(ICONST_0));
        insnPre.add(new InsnNode(BASTORE));
        insnPre.add(new VarInsnNode(ASTORE, 10));
        insnPre.add(new VarInsnNode(FLOAD, 1));
        insnPre.add(new VarInsnNode(FLOAD, 2));
        insnPre.add(new VarInsnNode(FLOAD, 3));
        insnPre.add(new VarInsnNode(ILOAD, 6));
        insnPre.add(new VarInsnNode(ALOAD, 10));
        insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPutColorMultiplier));
        insnPre.add(new VarInsnNode(ISTORE, 6));
        insnPre.add(new VarInsnNode(ALOAD, 10));
        insnPre.add(new InsnNode(ICONST_0));
        insnPre.add(new InsnNode(BALOAD));
        insnPre.add(new JumpInsnNode(IFNE, endJump));

        main.instructions.insertBefore(preNode, insnPre);
        main.instructions.insertBefore(postNode, endJump);
    }
}
