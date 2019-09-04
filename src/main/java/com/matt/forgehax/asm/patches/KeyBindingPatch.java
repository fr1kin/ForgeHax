package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import java.util.Objects;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class KeyBindingPatch extends ClassTransformer {

  public KeyBindingPatch() {
    super(Classes.KeyBinding);
  }

  @RegisterMethodTransformer
  private class IsKeyDown extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return Methods.KeyBinding_isKeyDown;
    }

    @Inject(description = "Shut down forge's shit for GuiMove")
    public void inject(MethodNode main) {
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
