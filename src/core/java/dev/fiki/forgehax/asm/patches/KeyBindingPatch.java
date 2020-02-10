package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import java.util.Objects;

import dev.fiki.forgehax.asm.TypesMc;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class KeyBindingPatch {

  @RegisterTransformer
  private static class IsKeyDown extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.KeyBinding_isKeyDown;
    }

    @Override
    public void transform(MethodNode main) {
      AbstractInsnNode node =
        ASMHelper.findPattern(
          main.instructions.getFirst(), new int[]{ALOAD, GETFIELD, IFEQ}, "xxx");
      
      Objects.requireNonNull(node, "Find pattern failed for getfield node");
      
      // Delete forge code
      AbstractInsnNode iteratorNode =
        node.getNext().getNext(); // set the iterator to the IFEQ instruction
      while (iteratorNode.getOpcode() != IRETURN) {
        iteratorNode = iteratorNode.getNext();
        main.instructions.remove(iteratorNode.getPrevious());
      }
    }
  }
}
