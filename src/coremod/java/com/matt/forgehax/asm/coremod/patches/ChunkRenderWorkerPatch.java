package com.matt.forgehax.asm.coremod.patches;

import static org.objectweb.asm.Opcodes.*;

import com.matt.forgehax.asm.coremod.TypesHook;
import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import com.matt.forgehax.asm.coremod.utils.asmtype.ASMMethod;

import java.util.Objects;
import java.util.Set;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/** Created on 5/11/2017 by fr1kin */
/*public class ChunkRenderWorkerPatch extends ClassTransformer {
  public ChunkRenderWorkerPatch() {
    super(Classes.ChunkRenderWorker);
  }

  @RegisterMethodTransformer
  private class FreeRenderBuilder extends MethodTransformer {
    @Override
    public ASMMethod getMethod() {
      return Methods.ChunkRenderWorker_freeRenderBuilder;
    }

    @Inject(description = "Add hook at the very top of the method")
    public void inject(MethodNode main) {
      AbstractInsnNode node = main.instructions.getFirst();

      Objects.requireNonNull(node, "Find pattern failed for node");

      InsnList insnList = new InsnList();
      insnList.add(new VarInsnNode(ALOAD, 1));
      insnList.add(
          ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onWorldRendererDeallocated));

      main.instructions.insertBefore(node, insnList);
    }
  }
}*/

public class ChunkRenderWorkerPatch {

  //@RegisterTransformer // TODO: fix?
  public static class FreeRenderBuilder implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<ITransformer.Target> targets() {
      return ASMHelper.getTargetSet(Methods.ChunkRenderWorker_freeRenderBuilder);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      AbstractInsnNode node = main.instructions.getFirst();

      Objects.requireNonNull(node, "Find pattern failed for node");

      InsnList insnList = new InsnList();
      insnList.add(new VarInsnNode(ALOAD, 1));
      insnList.add(
          ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onWorldRendererDeallocated));

      main.instructions.insertBefore(node, insnList);

      return main;
    }

  }
}
