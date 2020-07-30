package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.entity.player.PlayerEntity;
import org.objectweb.asm.tree.*;

@ClassMapping(PlayerEntity.class)
public class PlayerEntityPatch extends Patch {

  @Inject
  @MethodMapping("isStayingOnGroundSurface")
  public void isStayingOnGroundSurface(MethodNode method,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "shouldClipBlockEdge",
          args = {PlayerEntity.class},
          ret = boolean.class
      ) ASMMethod hook) {
    AbstractInsnNode ret = ASMPattern.builder()
        .codeOnly()
        .opcode(IRETURN)
        .find(method)
        .getFirst();

    LabelNode retLabel = new LabelNode();
    LabelNode label = new LabelNode();

    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(ASMHelper.call(INVOKESTATIC, hook));
    list.add(new JumpInsnNode(IFEQ, label));
    list.add(new InsnNode(ICONST_1));
    list.add(new JumpInsnNode(GOTO, retLabel));
    list.add(label);

    method.instructions.insert(list);
    method.instructions.insertBefore(ret, retLabel);
  }

  @Inject
  @MethodMapping("isPushedByWater")
  public void isPushedByWater(MethodNode method,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "shouldBePushedByLiquid",
          args = {PlayerEntity.class},
          ret = boolean.class
      ) ASMMethod hook) {
    AbstractInsnNode ret = ASMPattern.builder()
        .codeOnly()
        .opcode(IRETURN)
        .find(method)
        .getFirst();

    LabelNode end = new LabelNode();
    LabelNode skip = new LabelNode();

    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0)); // push PlayerEntity
    list.add(ASMHelper.call(INVOKESTATIC, hook));
    list.add(new JumpInsnNode(IFNE, skip)); // if not equal to 0 then player should be pushed
    list.add(new InsnNode(ICONST_0)); // will return false
    list.add(new JumpInsnNode(GOTO, end));
    list.add(skip); // player should be pushed, starting from this label will continue running the original code

    method.instructions.insert(list);
    method.instructions.insertBefore(ret, end);
  }
}
