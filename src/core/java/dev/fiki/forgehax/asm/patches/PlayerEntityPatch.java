package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.common.asmtype.ASMMethod;
import org.objectweb.asm.tree.*;

public class PlayerEntityPatch {
  @RegisterTransformer("ForgeHaxHooks::onPlayerEntitySneakEdgeCheck")
  public static class ShouldAvoidEdge extends MethodTransformer {
    @Override
    public ASMMethod getMethod() {
      return Methods.PlayerEntity_shouldAvoidEdgeFalling;
    }

    @Override
    public void transform(MethodNode method) {
      AbstractInsnNode ret = ASMPattern.builder()
          .codeOnly()
          .opcode(IRETURN)
          .find(method)
          .getFirst();

      LabelNode retLabel = new LabelNode();
      LabelNode label = new LabelNode();

      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerEntitySneakEdgeCheck));
      list.add(new JumpInsnNode(IFEQ, label));
      list.add(new InsnNode(ICONST_1));
      list.add(new JumpInsnNode(GOTO, retLabel));
      list.add(label);

      method.instructions.insert(list);
      method.instructions.insertBefore(ret, retLabel);
    }
  }
}
