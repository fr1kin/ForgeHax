package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.TypesHook;
import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class RenderChunkPatch {

  //@RegisterTransformer // this probably wont work
  public static class SetOpaqueCube implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<ITransformer.Target> targets() {
      return ASMHelper.getTargetSet(Methods.RenderChunk_rebuildChunk);
    }


    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      // searches for ++renderChunksUpdated;
      AbstractInsnNode top =
          ASMHelper.findPattern(
              main.instructions.getFirst(),
              new int[] {Opcodes.GETSTATIC, Opcodes.ICONST_1, Opcodes.IADD, Opcodes.PUTSTATIC},
              "xxxx");

      Objects.requireNonNull(top, "Find pattern failed for top");

      // somewhere within the block loop
      // Block block = iblockstate.getBlock();
      // <--- inject somewhere here
      // if (iblockstate.isOpaqueCube())
      AbstractInsnNode loop =
          ASMHelper.findPattern(
              top,
              new int[] {
                  Opcodes.ASTORE,
                  0x00,
                  0x00,
                  Opcodes.ALOAD,
                  Opcodes.INVOKEINTERFACE,
                  Opcodes.ASTORE,
                  0x00,
                  0x00,
                  Opcodes.ALOAD,
                  Opcodes.INVOKEINTERFACE,
                  Opcodes.IFEQ
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
      patch.add(new JumpInsnNode(Opcodes.IFNE, falseNode));
      patch.add(new InsnNode(Opcodes.ICONST_1));
      patch.add(new JumpInsnNode(Opcodes.GOTO, jumpPast));
      patch.add(falseNode);
      patch.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
      patch.add(new InsnNode(Opcodes.ICONST_0));
      patch.add(jumpPast);
      patch.add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER}));
      patch.add(new InsnNode(Opcodes.DUP));
      patch.add(new VarInsnNode(Opcodes.ISTORE, STORE_AT));
      patch.add(new JumpInsnNode(Opcodes.IFEQ, skipRenderingLabel));

      main.instructions.remove(extendedLevelCheckJumpNode); // remove IFNE
      main.instructions.insert(beforeJump, patch);

      // don't use this anymore
      extendedLevelCheckJumpNode = null;

      // ###############
      // PRE render hook
      // ###############

      InsnList pre = new InsnList();
      pre.add(new VarInsnNode(Opcodes.ALOAD, 0));
      pre.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPreBuildChunk));

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
      while (prev.getOpcode() != Opcodes.ALOAD) prev = prev.getPrevious();

      int BLOCK_POS_INDEX = ((VarInsnNode) prev).var;

      // now find block
      // should just be the next ASTORE starting at the loop node
      AbstractInsnNode next2 = loop.getNext();
      while (next2.getOpcode() != Opcodes.ASTORE) next2 = next2.getNext();

      int BLOCK_INDEX = ((VarInsnNode) next2).var;

      // keep next2 as we will inject the code after it

      InsnList list = new InsnList();
      list.add(new VarInsnNode(Opcodes.ALOAD, 0));
      list.add(new VarInsnNode(Opcodes.ALOAD, BLOCK_INDEX));
      list.add(new VarInsnNode(Opcodes.ALOAD, BLOCK_STATE_INDEX));
      list.add(new VarInsnNode(Opcodes.ALOAD, BLOCK_POS_INDEX));
      list.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onBlockRenderInLoop));

      main.instructions.insert(next2, list);

      // ###############
      // POST render hook
      // ###############

      LabelNode jumpOver = new LabelNode();

      InsnList post = new InsnList();
      post.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
      post.add(new VarInsnNode(Opcodes.ILOAD, STORE_AT));
      post.add(new JumpInsnNode(Opcodes.IFEQ, jumpOver));
      post.add(new VarInsnNode(Opcodes.ALOAD, 0));
      post.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPostBuildChunk));
      post.add(jumpOver);

      main.instructions.insert(skipRenderingLabel, post);

      return main;
    }

  }


  @RegisterTransformer
  public static class DeleteGlResources implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<ITransformer.Target> targets() {
      return ASMHelper.getTargetSet(Methods.RenderChunk_deleteGlResources);
    }


    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      AbstractInsnNode node = main.instructions.getFirst();

      Objects.requireNonNull(node, "Find pattern failed for node");

      InsnList insnList = new InsnList();
      insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
      insnList.add(
          ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onDeleteGlResources));

      main.instructions.insertBefore(node, insnList);

      return main;
    }

  }
}
