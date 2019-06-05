package com.matt.forgehax.asm.patches;

import static org.objectweb.asm.Opcodes.*;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.transformer.RegisterTransformer;
import com.matt.forgehax.asm.transformer.Transformer;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import java.util.Objects;
import java.util.Set;

import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;


public class ChunkRenderContainerPatch {
  @RegisterTransformer
  public static class OnStoppedUsingItem implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<Target> targets() {
      return ASMHelper.getTargetSet(Methods.ChunkRenderContainer_addRenderChunk);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      AbstractInsnNode node = main.instructions.getFirst();

      Objects.requireNonNull(node, "Find pattern failed for node");

      InsnList insnList = new InsnList();
      insnList.add(new VarInsnNode(ALOAD, 1));
      insnList.add(new VarInsnNode(ALOAD, 2));
      insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onAddRenderChunk));

      main.instructions.insertBefore(node, insnList);

      return main;
    }

  }

}
