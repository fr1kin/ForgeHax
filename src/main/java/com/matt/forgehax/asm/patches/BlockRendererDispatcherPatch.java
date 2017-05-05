package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmHelper;
import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.transforming.ClassTransformer;
import com.matt.forgehax.asm.helper.transforming.Inject;
import com.matt.forgehax.asm.helper.transforming.MethodTransformer;
import com.matt.forgehax.asm.helper.transforming.RegisterPatch;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created on 11/10/2016 by fr1kin
 */
public class BlockRendererDispatcherPatch extends ClassTransformer {
    public final AsmMethod ON_RENDER_BLOCK = new AsmMethod()
            .setName("renderBlock")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.IBLOCKSTATE, NAMES.BLOCKPOS, NAMES.IBLOCKACCESS, NAMES.VERTEXBUFFER)
            .setReturnType(boolean.class)
            .setHooks(NAMES.ON_RENDER_BLOCK);

    public BlockRendererDispatcherPatch() {
        super("net/minecraft/client/renderer/BlockRendererDispatcher");
    }

    @RegisterPatch
    private class ApplyBlockRender extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return ON_RENDER_BLOCK;
        }

        @Inject(description = "Inserts hook call")
        public void inject(MethodNode main) {
            AbstractInsnNode node = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {ALOAD, INVOKEINTERFACE}, "xx");

            Objects.requireNonNull(node, "Find pattern failed for node");

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 2));
            insnList.add(new VarInsnNode(ALOAD, 1));
            insnList.add(new VarInsnNode(ALOAD, 3));
            insnList.add(new VarInsnNode(ALOAD, 4));
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_RENDER_BLOCK.getParentClass().getRuntimeName(),
                    NAMES.ON_RENDER_BLOCK.getRuntimeName(),
                    NAMES.ON_RENDER_BLOCK.getDescriptor(),
                    false
            ));

            main.instructions.insertBefore(node, insnList);
        }
    }
}
