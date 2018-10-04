package com.matt.forgehax.asm.patches;

import static org.objectweb.asm.Opcodes.*;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import java.util.Objects;
import org.objectweb.asm.tree.*;

/** Created on 5/11/2017 by fr1kin */
public class ChunkRenderWorkerPatch extends ClassTransformer {
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
}
