package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.transforming.ClassTransformer;
import com.matt.forgehax.asm.helper.transforming.Inject;
import com.matt.forgehax.asm.helper.transforming.MethodTransformer;
import com.matt.forgehax.asm.helper.transforming.RegisterMethodTransformer;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created on 5/5/2017 by fr1kin
 */
public class BlockModelRendererPatch extends ClassTransformer {
    public final AsmMethod RENDER_MODEL = new AsmMethod()
            .setName("renderModel")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.IBLOCKACCESS, NAMES.IBAKEDMODEL, NAMES.IBLOCKSTATE, NAMES.BLOCKPOS, NAMES.VERTEXBUFFER, boolean.class, long.class)
            .setReturnType(boolean.class);

    public BlockModelRendererPatch() {
        super("net/minecraft/client/renderer/BlockModelRenderer");
    }

    @RegisterMethodTransformer
    private class RenderModel extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return RENDER_MODEL;
        }

        @Inject(description = "Block render callback")
        public void inject(MethodNode main) {
            AbstractInsnNode node = main.instructions.getFirst();

            Objects.requireNonNull(node, "Find pattern failed for node");

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 1));
            insnList.add(new VarInsnNode(ALOAD, 2));
            insnList.add(new VarInsnNode(ALOAD, 3));
            insnList.add(new VarInsnNode(ALOAD, 4));
            insnList.add(new VarInsnNode(ALOAD, 5));
            insnList.add(new VarInsnNode(ILOAD, 6));
            insnList.add(new VarInsnNode(LLOAD, 7));
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_BLOCK_MODEL_RENDER.getParentClass().getRuntimeName(),
                    NAMES.ON_BLOCK_MODEL_RENDER.getRuntimeName(),
                    NAMES.ON_BLOCK_MODEL_RENDER.getDescriptor(),
                    false
            ));

            main.instructions.insertBefore(node, insnList);
        }
    }
}
