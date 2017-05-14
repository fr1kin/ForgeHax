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
 * Created on 5/9/2017 by fr1kin
 */
public class ChunkRenderContainerPatch extends ClassTransformer {
    public final AsmMethod ADD_RENDERCHUNK = new AsmMethod()
            .setName("addRenderChunk")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.RENDER_CHUNK, NAMES.BLOCK_RENDER_LAYER)
            .setReturnType(void.class);

    public ChunkRenderContainerPatch() {
        super("net/minecraft/client/renderer/ChunkRenderContainer");
    }

    @RegisterMethodTransformer
    private class AddRenderChunk extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return ADD_RENDERCHUNK;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode node = main.instructions.getFirst();

            Objects.requireNonNull(node, "Find pattern failed for node");

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 1));
            insnList.add(new VarInsnNode(ALOAD, 2));
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_ADD_RENDERCHUNK.getParentClass().getRuntimeName(),
                    NAMES.ON_ADD_RENDERCHUNK.getRuntimeName(),
                    NAMES.ON_ADD_RENDERCHUNK.getDescriptor(),
                    false
            ));

            main.instructions.insertBefore(node, insnList);
        }
    }
}
