package com.matt.forgehax.asm.patches;

import com.google.common.collect.Lists;
import com.matt.forgehax.asm.helper.AsmField;
import com.matt.forgehax.asm.helper.AsmHelper;
import com.matt.forgehax.asm.helper.AsmMethod;
import com.matt.forgehax.asm.helper.transforming.ClassTransformer;
import com.matt.forgehax.asm.helper.transforming.Inject;
import com.matt.forgehax.asm.helper.transforming.MethodTransformer;
import com.matt.forgehax.asm.helper.transforming.RegisterMethodTransformer;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created on 5/5/2017 by fr1kin
 */
public class RenderChunkPatch extends ClassTransformer {
    public final AsmMethod REBUILD_CHUNK = new AsmMethod()
            .setName("rebuildChunk")
            .setObfuscatedName("b")
            .setArgumentTypes(float.class, float.class, float.class, NAMES.CHUNK_COMPILE_TASK_GENERATOR)
            .setReturnType(void.class);

    public final AsmMethod DELETE_GL_RESOURCES = new AsmMethod()
            .setName("deleteGlResources")
            .setObfuscatedName("a")
            .setArgumentTypes()
            .setReturnType(void.class);

    public final AsmMethod PRE_BLOCK_MODEL_RENDER = new AsmMethod()
            .setName("preRenderBlocks")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.VERTEXBUFFER, NAMES.BLOCKPOS)
            .setReturnType(void.class);

    public final AsmMethod POST_BLOCK_MODEL_RENDER = new AsmMethod()
            .setName("postRenderBlocks")
            .setObfuscatedName("a")
            .setArgumentTypes(NAMES.BLOCK_RENDER_LAYER, float.class, float.class, float.class, NAMES.VERTEXBUFFER, NAMES.COMPILED_CHUNK)
            .setReturnType(void.class);

    public RenderChunkPatch() {
        super("net/minecraft/client/renderer/chunk/RenderChunk");
    }

    @RegisterMethodTransformer
    private class RebuildChunk extends MethodTransformer {
        public final AsmField REGION = new AsmField()
                .setParentClass(NAMES.RENDER_CHUNK)
                .setName("region")
                .setObfuscatedName("r")
                .setType(NAMES.CHUNK_CACHE);

        @Override
        public AsmMethod getMethod() {
            return REBUILD_CHUNK;
        }

        @Inject(description = "Add hooks before and after blocks are added to the buffer")
        public void inject(MethodNode main) {
            // searches for ++renderChunksUpdated;
            AbstractInsnNode top = AsmHelper.findPattern(main.instructions.getFirst(), new int[]{
                    GETSTATIC, ICONST_1, IADD, PUTSTATIC
            }, "xxxx");

            Objects.requireNonNull(top, "Find pattern failed for top");

            // somewhere within the block loop
            //Block block = iblockstate.getBlock();
            // <--- inject somewhere here
            //if (iblockstate.isOpaqueCube())
            AbstractInsnNode loop = AsmHelper.findPattern(top, new int[]{
                    ASTORE,
                    0x00, 0x00,
                    ALOAD, INVOKEINTERFACE, ASTORE,
                    0x00, 0x00,
                    ALOAD, INVOKEINTERFACE, IFEQ
            }, "x??xxx??xxx");

            Objects.requireNonNull(loop, "Find pattern failed for loop");

            // Find the if (!this.region.extendedLevelsInChunkCache()) jump opcode
            AbstractInsnNode last = top;
            while (last != null && !(last instanceof JumpInsnNode)) {
                last = last.getPrevious();
            }
            // we should be at IFNE
            Objects.requireNonNull(last, "Failed to find jump node for 'last'");
            JumpInsnNode extendedLevelCheckJumpNode = (JumpInsnNode) last;
            LabelNode skipRenderingLabel = extendedLevelCheckJumpNode.label;

            int STORE_AT = main.maxLocals++;

            // REQUIRED to compute correct max values
            main.visitMaxs(0, 0);

            // changing if (!this.region.extendedLevelsInChunkCache())
            // from (will differ with optifine, but that is ok)
            // >> ALOAD 0
            // >> GETFIELD net/minecraft/client/renderer/chunk/RenderChunk.region : Lnet/minecraft/world/ChunkCache;
            // >> INVOKEVIRTUAL net/minecraft/world/ChunkCache.extendedLevelsInChunkCache ()Z
            // >> IFNE L20
            // to if(var = !this.region.extendedLevelsInChunkCache())
            // >> ALOAD 0
            // >> GETFIELD net/minecraft/client/renderer/chunk/RenderChunk.region : Lnet/minecraft/world/ChunkCache;
            // >> INVOKEVIRTUAL net/minecraft/world/ChunkCache.extendedLevelsInChunkCache ()Z
            // >> IFNE falseNode
            // >> ICONST_1
            // >> GOTO jumpPast
            // >> falseNode:
            // >> F_SAME
            // >> ICONST_0
            // >> jumpPast:
            // >> F_SAME1 I
            // >> DUP
            // >> ISTORE
            // >> IFEQ

            AbstractInsnNode beforeJump = extendedLevelCheckJumpNode.getPrevious();

            LabelNode jumpPast = new LabelNode();
            LabelNode falseNode = new LabelNode();

            InsnList patch = new InsnList();
            patch.add(new JumpInsnNode(IFNE, falseNode));
            patch.add(new InsnNode(ICONST_1));
            patch.add(new JumpInsnNode(GOTO, jumpPast));
            patch.add(falseNode);
            patch.add(new FrameNode(F_SAME, 0, null, 0, null));
            patch.add(new InsnNode(ICONST_0));
            patch.add(jumpPast);
            patch.add(new FrameNode(F_SAME1, 0, null, 1, new Object[]{INTEGER}));
            patch.add(new InsnNode(DUP));
            patch.add(new VarInsnNode(ISTORE, STORE_AT));
            patch.add(new JumpInsnNode(IFEQ, skipRenderingLabel));

            main.instructions.remove(extendedLevelCheckJumpNode); // remove IFNE
            main.instructions.insert(beforeJump, patch);

            // don't use this anymore
            extendedLevelCheckJumpNode = null;

            // ###############
            // PRE render hook
            // ###############

            InsnList pre = new InsnList();
            pre.add(new VarInsnNode(ALOAD, 0));
            pre.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_PRE_BUILD_CHUNK.getParentClass().getRuntimeName(),
                    NAMES.ON_PRE_BUILD_CHUNK.getRuntimeName(),
                    NAMES.ON_PRE_BUILD_CHUNK.getDescriptor(),
                    false
            ));

            main.instructions.insertBefore(top, pre);

            // ###############
            // block render hook
            // ###############

            // required 3 args: blockpos, blockstate, block

            // 1st and easiest, store the index of the block state object
            int BLOCK_STATE_INDEX = ((VarInsnNode)loop).var;

            // now find blockpos

            // since the pattern started after storing IBlockState we go back and search
            // IBlockState iblockstate = this.region.getBlockState(blockpos$mutableblockpos);
            // for the index of blockpos$mutableblockpos, which SHOULD be the last ALOAD
            // i have to do this because optifine changes up the code at that line
            AbstractInsnNode prev = loop;
            while(prev.getOpcode() != ALOAD) prev = prev.getPrevious();

            int BLOCK_POS_INDEX = ((VarInsnNode)prev).var;

            // now find block
            // should just be the next ASTORE starting at the loop node
            AbstractInsnNode next2 = loop.getNext();
            while (next2.getOpcode() != ASTORE) next2 = next2.getNext();

            int BLOCK_INDEX = ((VarInsnNode)next2).var;

            // keep next2 as we will inject the code after it

            InsnList list = new InsnList();
            list.add(new VarInsnNode(ALOAD, 0));
            list.add(new VarInsnNode(ALOAD, BLOCK_INDEX));
            list.add(new VarInsnNode(ALOAD, BLOCK_STATE_INDEX));
            list.add(new VarInsnNode(ALOAD, BLOCK_POS_INDEX));
            list.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_BLOCK_RENDER_IN_LOOP.getParentClass().getRuntimeName(),
                    NAMES.ON_BLOCK_RENDER_IN_LOOP.getRuntimeName(),
                    NAMES.ON_BLOCK_RENDER_IN_LOOP.getDescriptor(),
                    false
            ));

            main.instructions.insert(next2, list);

            // ###############
            // POST render hook
            // ###############

            LabelNode jumpOver = new LabelNode();

            InsnList post = new InsnList();
            post.add(new FrameNode(F_SAME, 0, null, 0, null));
            post.add(new VarInsnNode(ILOAD, STORE_AT));
            post.add(new JumpInsnNode(IFEQ, jumpOver));
            post.add(new VarInsnNode(ALOAD, 0));
            post.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_POST_BUILD_CHUNK.getParentClass().getRuntimeName(),
                    NAMES.ON_POST_BUILD_CHUNK.getRuntimeName(),
                    NAMES.ON_POST_BUILD_CHUNK.getDescriptor(),
                    false
            ));
            post.add(jumpOver);

            main.instructions.insert(skipRenderingLabel, post);
        }
    }

    @RegisterMethodTransformer
    private class DeleteGlResources extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return DELETE_GL_RESOURCES;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode node = main.instructions.getFirst();

            Objects.requireNonNull(node, "Find pattern failed for node");

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 0));
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_DELETE_GL_RESOURCES.getParentClass().getRuntimeName(),
                    NAMES.ON_DELETE_GL_RESOURCES.getRuntimeName(),
                    NAMES.ON_DELETE_GL_RESOURCES.getDescriptor(),
                    false
            ));

            main.instructions.insertBefore(node, insnList);
        }
    }

    @RegisterMethodTransformer
    private class PreBlockModelRender extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return PRE_BLOCK_MODEL_RENDER;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode node = main.instructions.getFirst();

            Objects.requireNonNull(node, "Find pattern failed for node");

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 0));
            insnList.add(new VarInsnNode(ALOAD, 1));
            insnList.add(new VarInsnNode(ALOAD, 2));
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_PRE_BLOCK_MODEL_RENDER.getParentClass().getRuntimeName(),
                    NAMES.ON_PRE_BLOCK_MODEL_RENDER.getRuntimeName(),
                    NAMES.ON_PRE_BLOCK_MODEL_RENDER.getDescriptor(),
                    false
            ));

            main.instructions.insertBefore(node, insnList);
        }
    }

    @RegisterMethodTransformer
    private class PostBlockModelRender extends MethodTransformer {
        @Override
        public AsmMethod getMethod() {
            return POST_BLOCK_MODEL_RENDER;
        }

        @Inject
        public void inject(MethodNode main) {
            AbstractInsnNode node = main.instructions.getFirst();

            Objects.requireNonNull(node, "Find pattern failed for node");

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 0));
            insnList.add(new VarInsnNode(ALOAD, 5));
            insnList.add(new VarInsnNode(FLOAD, 2));
            insnList.add(new VarInsnNode(FLOAD, 3));
            insnList.add(new VarInsnNode(FLOAD, 4));
            insnList.add(new MethodInsnNode(INVOKESTATIC,
                    NAMES.ON_POST_BLOCK_MODEL_RENDER.getParentClass().getRuntimeName(),
                    NAMES.ON_POST_BLOCK_MODEL_RENDER.getRuntimeName(),
                    NAMES.ON_POST_BLOCK_MODEL_RENDER.getDescriptor(),
                    false
            ));

            main.instructions.insertBefore(node, insnList);
        }
    }
}
