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

/** Created on 5/7/2017 by fr1kin */
public class ChunkRenderDispatcherPatch extends ClassTransformer {
  public ChunkRenderDispatcherPatch() {
    super(Classes.ChunkRenderDispatcher);
  }

  @RegisterMethodTransformer
  private class UploadChunk extends MethodTransformer {
    @Override
    public ASMMethod getMethod() {
      return Methods.ChunkRenderDispatcher_uploadChunk;
    }

    @Inject(description = "Insert hook before buffer is uploaded")
    public void inject(MethodNode main) {
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
    }
  }
}
