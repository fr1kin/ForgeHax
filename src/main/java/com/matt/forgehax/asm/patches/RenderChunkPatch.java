package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import java.util.Objects;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Created on 5/5/2017 by fr1kin
 */
public class RenderChunkPatch extends ClassTransformer {

  public RenderChunkPatch() {
    super(Classes.RenderChunk);
  }

  @RegisterMethodTransformer
  private class RebuildChunk extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return Methods.RenderChunk_rebuildChunk;
    }

    @Inject(description = "Add hooks before and after blocks are added to the buffer")
    public void inject(MethodNode main) {
      // searches for ++renderChunksUpdated;
      AbstractInsnNode top =
        ASMHelper.findPattern(
          main.instructions.getFirst(),
          new int[]{GETSTATIC, ICONST_1, IADD, PUTSTATIC},
          "xxxx");

      Objects.requireNonNull(top, "Find pattern failed for top");

      // somewhere within the block loop
      // Block block = iblockstate.getBlock();
      // <--- inject somewhere here
      // if (iblockstate.isOpaqueCube())
      AbstractInsnNode loop =
        ASMHelper.findPattern(
          top,
          new int[]{
            ASTORE,
            0x00,
            0x00,
            ALOAD,
            INVOKEINTERFACE,
            ASTORE,
            0x00,
            0x00,
            ALOAD,
            INVOKEINTERFACE,
            IFEQ
          },
          "x??xxx??xxx");

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
      // >> GETFIELD net/minecraft/client/renderer/chunk/RenderChunk.region :
      // Lnet/minecraft/world/ChunkCache;
      // >> INVOKEVIRTUAL net/minecraft/world/ChunkCache.extendedLevelsInChunkCache ()Z
      // >> IFNE L20
      // to if(var = !this.region.extendedLevelsInChunkCache())
      // >> ALOAD 0
      // >> GETFIELD net/minecraft/client/renderer/chunk/RenderChunk.region :
      // Lnet/minecraft/world/ChunkCache;
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
      pre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPreBuildChunk));

      main.instructions.insertBefore(top, pre);

      // ###############
      // block render hook
      // ###############

      // required 3 args: blockpos, blockstate, block

      // 1st and easiest, store the index of the block state object
      int BLOCK_STATE_INDEX = ((VarInsnNode) loop).var;

      // now find blockpos

      // since the pattern started after storing IBlockState we go back and search
      // IBlockState iblockstate = this.region.getBlockState(blockpos$mutableblockpos);
      // for the index of blockpos$mutableblockpos, which SHOULD be the last ALOAD
      // i have to do this because optifine changes up the code at that line
      AbstractInsnNode prev = loop;
      while (prev.getOpcode() != ALOAD) {
        prev = prev.getPrevious();
      }

      int BLOCK_POS_INDEX = ((VarInsnNode) prev).var;

      // now find block
      // should just be the next ASTORE starting at the loop node
      AbstractInsnNode next2 = loop.getNext();
      while (next2.getOpcode() != ASTORE) {
        next2 = next2.getNext();
      }

      int BLOCK_INDEX = ((VarInsnNode) next2).var;

      // keep next2 as we will inject the code after it

      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(new VarInsnNode(ALOAD, BLOCK_INDEX));
      list.add(new VarInsnNode(ALOAD, BLOCK_STATE_INDEX));
      list.add(new VarInsnNode(ALOAD, BLOCK_POS_INDEX));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onBlockRenderInLoop));

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
      post.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPostBuildChunk));
      post.add(jumpOver);

      main.instructions.insert(skipRenderingLabel, post);
    }
  }

  @RegisterMethodTransformer
  private class DeleteGlResources extends MethodTransformer {
  
    @Override
    public ASMMethod getMethod() {
      return Methods.RenderChunk_deleteGlResources;
    }

    @Inject(description = "Add hook callback at top of method")
    public void inject(MethodNode main) {
      AbstractInsnNode node = main.instructions.getFirst();

      Objects.requireNonNull(node, "Find pattern failed for node");

      InsnList insnList = new InsnList();
      insnList.add(new VarInsnNode(ALOAD, 0));
      insnList.add(
        ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onDeleteGlResources));

      main.instructions.insertBefore(node, insnList);
    }
  }
}
