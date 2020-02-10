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
import org.objectweb.asm.tree.VarInsnNode;

public class PlayerControllerPatch {

  @RegisterTransformer("ForgeHaxHooks::onPlayerItemSync")
  public static class SyncCurrentPlayItem extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.PlayerController_syncCurrentPlayItem;
    }

    @Override
    public void transform(MethodNode node) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerItemSync));

      node.instructions.insert(list);
    }
  }

  @RegisterTransformer("ForgeHaxHooks::onPlayerAttackEntity")
  public static class AttackEntity extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.PlayerController_attackEntity;
    }

    @Override
    public void transform(MethodNode node) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(new VarInsnNode(ALOAD, 1));
      list.add(new VarInsnNode(ALOAD, 2));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerAttackEntity));

      node.instructions.insert(list);
    }
  }

  @RegisterTransformer("ForgeHaxHooks::onPlayerBreakingBlock")
  public static class OnPlayerDamageBlock extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.PlayerController_onPlayerDamageBlock;
    }

    @Override
    public void transform(MethodNode node) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(new VarInsnNode(ALOAD, 1));
      list.add(new VarInsnNode(ALOAD, 2));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerBreakingBlock));

      node.instructions.insert(list);
    }
  }

  @RegisterTransformer("ForgeHaxHooks::onPlayerStopUse")
  public static class OnStoppedUsingItem extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.PlayerController_onStoppedUsingItem;
    }

    @Override
    public void transform(MethodNode node) {
      AbstractInsnNode last =
          ASMHelper.findPattern(node.instructions.getFirst(), new int[]{RETURN}, "x");

      Objects.requireNonNull(last, "Could not find RET opcode");

      LabelNode label = new LabelNode();

      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(new VarInsnNode(ALOAD, 1));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerStopUse));
      list.add(new JumpInsnNode(IFNE, label));

      node.instructions.insert(list);
      node.instructions.insertBefore(last, label);
    }
  }
}
