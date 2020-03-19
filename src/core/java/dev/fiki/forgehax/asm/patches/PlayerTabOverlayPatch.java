package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.InsnPattern;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.common.asmtype.ASMMethod;
import org.objectweb.asm.tree.*;

/**
 * Created by Babbaj on 8/9/2017. thanks 086 :3
 */
public class PlayerTabOverlayPatch {
  @RegisterTransformer("ForgeHaxHooks::doIncreaseTabListSize")
  public static class RenderPlayerListRenderIcon extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.PlayerTabOverlayGui_renderPlayerList;
    }

    @Override
    public void transform(MethodNode main) {
      InsnPattern nodes = ASMPattern.builder()
          .codeOnly()
          .opcodes(ALOAD,
              ICONST_0,
              ALOAD,
              INVOKEINTERFACE,
              BIPUSH,
              INVOKESTATIC,
              INVOKEINTERFACE,
              ASTORE)
          .find(main);

      AbstractInsnNode subListNode = nodes.getFirst();
      AbstractInsnNode post = nodes.getLast();

      LabelNode jump = new LabelNode();

      InsnList insnList = new InsnList();
      insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldIncreaseTabListSize));
      insnList.add(new JumpInsnNode(IFNE, jump));

      main.instructions.insertBefore(subListNode, insnList);
      main.instructions.insert(post, jump);
    }
  }
}
