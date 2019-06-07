package com.matt.forgehax.asm.coremod.patches;

import static org.objectweb.asm.Opcodes.*;

import com.matt.forgehax.asm.coremod.TypesHook;
import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import java.util.Objects;
import java.util.Set;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;


public class ChunkRenderDispatcherPatch {

  @RegisterTransformer
  public static class UploadChunk implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<ITransformer.Target> targets() {
      return ASMHelper.getTargetSet(Methods.ChunkRenderDispatcher_uploadChunk);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      AbstractInsnNode node =
          ASMHelper.findPattern(
              main.instructions.getFirst(),
              new int[] {
                  INVOKESTATIC, IFEQ, 0x00, 0x00, ALOAD,
              },
              "xx??x");

      Objects.requireNonNull(node, "Find pattern failed for node");

      InsnList insnList = new InsnList();
      insnList.add(new VarInsnNode(ALOAD, 3));
      insnList.add(new VarInsnNode(ALOAD, 2));
      insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onChunkUploaded));

      main.instructions.insertBefore(node, insnList);

      return main;
    }
  }
}
