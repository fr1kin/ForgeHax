package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.helper.AsmHelper;
import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.transforming.ClassTransformer;
import com.matt.forgehax.asm.helper.transforming.MethodTransformer;
import com.matt.forgehax.asm.helper.transforming.RegisterPatch;
import com.matt.forgehax.asm.helper.transforming.Inject;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class BlockPatch extends ClassTransformer {
    public final AsmMethod CAN_RENDER_IN_LAYER = new AsmMethod()
            .setName("canRenderInLayer")
            .setObfuscatedName("canRenderInLayer")
            .setArgumentTypes(NAMES.IBLOCKSTATE, NAMES.BLOCK_RENDER_LAYER)
            .setReturnType(boolean.class)
            .setHooks(NAMES.ON_RENDERBLOCK_INLAYER);

    public final AsmMethod ADD_COLLISION_BOX_TO_LIST = new AsmMethod()
            .setName("addCollisionBoxToList")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.IBLOCKSTATE, NAMES.WORLD, NAMES.BLOCKPOS, NAMES.AXISALIGNEDBB, List.class, NAMES.ENTITY, boolean.class)
            .setReturnType(void.class);

    public BlockPatch() {
        super("net/minecraft/block/Block");
    }

    @RegisterPatch
    private class CanRenderInLayer extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return CAN_RENDER_IN_LAYER;
        }

        @Inject(description = "Changes in layer code so that we can change it")
        public void inject(MethodNode main) {
            AbstractInsnNode node = AsmHelper.findPattern(main.instructions.getFirst(), new int[]{INVOKEVIRTUAL}, "x");

            Objects.requireNonNull(node, "Find pattern failed for node");

            InsnList insnList = new InsnList();

            // starting after INVOKEVIRTUAL on Block.getBlockLayer()

            insnList.add(new VarInsnNode(ASTORE, 3)); // store the result from getBlockLayer()
            insnList.add(new VarInsnNode(ALOAD, 0)); // push this
            insnList.add(new VarInsnNode(ALOAD, 1)); // push block state
            insnList.add(new VarInsnNode(ALOAD, 3)); // push this.getBlockLayer() result
            insnList.add(new VarInsnNode(ALOAD, 2)); // push the block layer of the block we are comparing to
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_RENDERBLOCK_INLAYER.getParentClass().getRuntimeName(),
                    NAMES.ON_RENDERBLOCK_INLAYER.getRuntimeName(),
                    NAMES.ON_RENDERBLOCK_INLAYER.getDescriptor(),
                    false
            ));
            // now our result is on the stack

            main.instructions.insert(node, insnList);
        }
    }

    @RegisterPatch
    private class AddCollisionBoxToList extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return ADD_COLLISION_BOX_TO_LIST;
        }

        @Inject(description = "Inserts hook call")
        public void inject(MethodNode main) {
            AbstractInsnNode node = AsmHelper.findPattern(main.instructions.getFirst(), new int[] {ALOAD}, "x");

            Objects.requireNonNull(node, "Find pattern failed for node");

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 0)); //this
            insnList.add(new VarInsnNode(ALOAD, 1)); //state
            insnList.add(new VarInsnNode(ALOAD, 2)); //world
            insnList.add(new VarInsnNode(ALOAD, 5)); //list
            insnList.add(new VarInsnNode(ALOAD, 3)); //pos
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_BLOCK_ADD_COLLISION.getParentClass().getRuntimeName(),
                    NAMES.ON_BLOCK_ADD_COLLISION.getRuntimeName(),
                    NAMES.ON_BLOCK_ADD_COLLISION.getDescriptor(),
                    false
            ));

            main.instructions.insert(node, insnList);
        }
    }
}
