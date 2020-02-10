package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import java.util.Objects;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Created by Babbaj on 8/9/2017. thanks 086 :3
 */
public class PlayerTabOverlayPatch {


  @RegisterTransformer
  private static class RenderPlayerlist_renderIcon extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.PlayerTabOverlayGui_renderPlayerList;
    }

    @Override
    public void transform(MethodNode main) {
      AbstractInsnNode subListNode =
          ASMHelper.findPattern(
              main.instructions.getFirst(),
              new int[]{
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
