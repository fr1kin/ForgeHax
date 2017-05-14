package com.matt.forgehax.asm.patches;

import com.google.common.util.concurrent.ListenableFuture;
import com.matt.forgehax.asm.helper.AsmHelper;
import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.transforming.ClassTransformer;
import com.matt.forgehax.asm.helper.transforming.Inject;
import com.matt.forgehax.asm.helper.transforming.MethodTransformer;
import com.matt.forgehax.asm.helper.transforming.RegisterMethodTransformer;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created on 5/7/2017 by fr1kin
 */
public class ChunkRenderDispatcherPatch extends ClassTransformer {
    public final AsmMethod UPLOAD_CHUNK = new AsmMethod()
            .setName("uploadChunk")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.BLOCK_RENDER_LAYER, NAMES.VERTEXBUFFER, NAMES.RENDER_CHUNK, NAMES.COMPILED_CHUNK, double.class)
            .setReturnType(ListenableFuture.class);

    public ChunkRenderDispatcherPatch() {
        super("net/minecraft/client/renderer/chunk/ChunkRenderDispatcher");
    }

    @RegisterMethodTransformer
    private class UploadChunk extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return UPLOAD_CHUNK;
        }

        @Inject(description = "Insert hook before buffer is uploaded")
        public void inject(MethodNode main) {
            AbstractInsnNode node = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    INVOKESTATIC, IFEQ,
                    0x00, 0x00,
                    ALOAD,
            }, "xx??x");

            Objects.requireNonNull(node, "Find pattern failed for node");

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 3));
            insnList.add(new VarInsnNode(ALOAD, 2));
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_CHUNK_UPLOADED.getParentClass().getRuntimeName(),
                    NAMES.ON_CHUNK_UPLOADED.getRuntimeName(),
                    NAMES.ON_CHUNK_UPLOADED.getDescriptor(),
                    false
            ));

            main.instructions.insertBefore(node, insnList);
        }
    }
}
