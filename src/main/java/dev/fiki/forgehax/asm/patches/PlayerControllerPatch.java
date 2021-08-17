package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.asm.MapClass;
import dev.fiki.forgehax.api.asm.MapMethod;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.client.multiplayer.PlayerController;
import org.objectweb.asm.tree.*;

import java.util.Objects;

@MapClass(PlayerController.class)
public class PlayerControllerPatch extends Patch {

  @Inject
  @MapMethod("ensureHasSentCarriedItem")
  public void ensureHasSentCarriedItem(MethodNode node,
      @MapMethod(parentClass = ForgeHaxHooks.class, name = "onPlayerItemSync") ASMMethod hook) {
    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(ASMHelper.call(INVOKESTATIC, hook));

    node.instructions.insert(list);
  }

  @Inject
  @MapMethod("attack")
  public void attack(MethodNode node,
      @MapMethod(parentClass = ForgeHaxHooks.class, name = "onPlayerAttackEntity") ASMMethod hook) {
    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(new VarInsnNode(ALOAD, 1));
    list.add(new VarInsnNode(ALOAD, 2));
    list.add(ASMHelper.call(INVOKESTATIC, hook));

    node.instructions.insert(list);
  }

  @Inject
  @MapMethod("continueDestroyBlock")
  public void continueDestroyBlock(MethodNode node,
      @MapMethod(parentClass = ForgeHaxHooks.class, name = "onPlayerBreakingBlock") ASMMethod hook) {
    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(new VarInsnNode(ALOAD, 1));
    list.add(new VarInsnNode(ALOAD, 2));
    list.add(ASMHelper.call(INVOKESTATIC, hook));

    node.instructions.insert(list);
  }

  @Inject
  @MapMethod("releaseUsingItem")
  public void releaseUsingItem(MethodNode node,
      @MapMethod(parentClass = ForgeHaxHooks.class, name = "onPlayerStopUse") ASMMethod hook) {
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
