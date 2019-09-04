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
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Created on 5/9/2017 by fr1kin
 */
public class ChunkRenderContainerPatch extends ClassTransformer {
  
  public ChunkRenderContainerPatch() {
    super(Classes.ChunkRenderContainer);
  }

  @RegisterMethodTransformer
  private class AddRenderChunk extends MethodTransformer {
  
    @Override
    public ASMMethod getMethod() {
      return Methods.ChunkRenderContainer_addRenderChunk;
    }

    @Inject
    public void inject(MethodNode main) {
      AbstractInsnNode node = main.instructions.getFirst();

      Objects.requireNonNull(node, "Find pattern failed for node");

      InsnList insnList = new InsnList();
      insnList.add(new VarInsnNode(ALOAD, 1));
      insnList.add(new VarInsnNode(ALOAD, 2));
      insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onAddRenderChunk));

      main.instructions.insertBefore(node, insnList);
    }
  }
}
