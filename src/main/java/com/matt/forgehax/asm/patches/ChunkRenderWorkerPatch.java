package com.matt.forgehax.asm.patches;

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
 * Created on 5/11/2017 by fr1kin
 */
public class ChunkRenderWorkerPatch extends ClassTransformer {
    public final AsmMethod PROCESS_TASK = new AsmMethod()
            .setName("processTask")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.CHUNK_COMPILE_TASK_GENERATOR)
            .setReturnType(void.class);

    public final AsmMethod FREE_RENDER_BUILDER = new AsmMethod()
            .setName("freeRenderBuilder")
            .setObfuscatedName("b")
            .setArgumentTypes(NAMES.CHUNK_COMPILE_TASK_GENERATOR)
            .setReturnType(void.class);

    public ChunkRenderWorkerPatch() {
        super("net/minecraft/client/renderer/chunk/ChunkRenderWorker");
    }

    //@RegisterMethodTransformer
    private class ProcessTask extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return PROCESS_TASK;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode node = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {
                    ALOAD, ALOAD, INVOKESPECIAL, INVOKEVIRTUAL,
                    0x00, 0x00,
                    ALOAD, GETFIELD, D2F, FSTORE
            }, "xxxx??xxxx");

            Objects.requireNonNull(node, "Find pattern failed for node");

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 1));
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_WORLDRENDERER_ALLOCATED.getParentClass().getRuntimeName(),
                    NAMES.ON_WORLDRENDERER_ALLOCATED.getRuntimeName(),
                    NAMES.ON_WORLDRENDERER_ALLOCATED.getDescriptor(),
                    false
            ));

            main.instructions.insertBefore(node, insnList);
        }
    }

    @RegisterMethodTransformer
    private class FreeRenderBuilder extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return FREE_RENDER_BUILDER;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode node = main.instructions.getFirst();

            Objects.requireNonNull(node, "Find pattern failed for node");

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 1));
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_WORLDRENDERER_DEALLOCATED.getParentClass().getRuntimeName(),
                    NAMES.ON_WORLDRENDERER_DEALLOCATED.getRuntimeName(),
                    NAMES.ON_WORLDRENDERER_DEALLOCATED.getDescriptor(),
                    false
            ));

            main.instructions.insertBefore(node, insnList);
        }
    }
}
