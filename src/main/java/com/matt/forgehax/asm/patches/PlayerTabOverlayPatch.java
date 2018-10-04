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

/** Created by Babbaj on 8/9/2017. thanks 086 :3 */
public class PlayerTabOverlayPatch extends ClassTransformer {
  public PlayerTabOverlayPatch() {
    super(Classes.GuiPlayerTabOverlay);
  }

  @RegisterMethodTransformer
  private class RenderPlayerlist extends MethodTransformer {
    @Override
    public ASMMethod getMethod() {
      return Methods.PlayerTabOverlay_renderPlayerList;
    }

    @Inject(description = "Add hook to increase the size of the tab list")
    public void inject(MethodNode main) {
      AbstractInsnNode subListNode =
          ASMHelper.findPattern(
              main.instructions.getFirst(),
              new int[] {
                ALOAD,
                ICONST_0,
                ALOAD,
                INVOKEINTERFACE,
                BIPUSH,
                INVOKESTATIC,
                INVOKEINTERFACE,
                ASTORE
              },
              "xxxxxxxx");

      AbstractInsnNode astoreNode = subListNode;
      for (int i = 0; i < 7; i++) {
        astoreNode = astoreNode.getNext();
      }

      Objects.requireNonNull(subListNode, "Find pattern failed for subList");
      Objects.requireNonNull(astoreNode, "Find pattern failed for subListPost");

      LabelNode jump = new LabelNode();

      InsnList insnList = new InsnList();
      insnList.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_doIncreaseTabListSize));
      insnList.add(new JumpInsnNode(IFNE, jump));

      main.instructions.insertBefore(subListNode, insnList);
      main.instructions.insert(astoreNode, jump);
    }
  }
}
