package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.tree.*;

import java.util.Objects;

@ClassMapping(PlayerController.class)
public class PlayerControllerPatch extends Patch {

  @Inject
  @MethodMapping("syncCurrentPlayItem")
  public void syncCurrentPlayItem(MethodNode node,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "onPlayerItemSync",
          args = {PlayerController.class},
          ret = void.class
      ) ASMMethod hook) {
    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(ASMHelper.call(INVOKESTATIC, hook));

    node.instructions.insert(list);
  }

  @Inject
  @MethodMapping("attackEntity")
  public void attackEntity(MethodNode node,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "onPlayerAttackEntity",
          args = {PlayerController.class, PlayerEntity.class, Entity.class},
          ret = void.class
      ) ASMMethod hook) {
    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(new VarInsnNode(ALOAD, 1));
    list.add(new VarInsnNode(ALOAD, 2));
    list.add(ASMHelper.call(INVOKESTATIC, hook));

    node.instructions.insert(list);
  }

  @Inject
  @MethodMapping("onPlayerDamageBlock")
  public void onPlayerDamageBlock(MethodNode node,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "onPlayerBreakingBlock",
          args = {PlayerController.class, BlockPos.class, Direction.class},
          ret = void.class
      ) ASMMethod hook) {
    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(new VarInsnNode(ALOAD, 1));
    list.add(new VarInsnNode(ALOAD, 2));
    list.add(ASMHelper.call(INVOKESTATIC, hook));

    node.instructions.insert(list);
  }

  @Inject
  @MethodMapping("onStoppedUsingItem")
  public void transform(MethodNode node,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "onPlayerStopUse",
          args = {PlayerController.class, PlayerEntity.class},
          ret = boolean.class
      ) ASMMethod hook) {
    AbstractInsnNode last = ASMHelper.findPattern(node.instructions.getFirst(), new int[]{RETURN}, "x");

    Objects.requireNonNull(last, "Could not find RET opcode");

    LabelNode label = new LabelNode();

    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(new VarInsnNode(ALOAD, 1));
    list.add(ASMHelper.call(INVOKESTATIC, hook));
    list.add(new JumpInsnNode(IFNE, label));

    node.instructions.insert(list);
    node.instructions.insertBefore(last, label);
  }
}
