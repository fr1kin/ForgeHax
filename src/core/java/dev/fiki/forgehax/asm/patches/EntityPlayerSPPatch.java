package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import java.util.Objects;

import dev.fiki.forgehax.asm.TypesMc;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Created on 11/13/2016 by fr1kin
 */
public class EntityPlayerSPPatch {

  @RegisterTransformer
  public static class ApplyLivingUpdate extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.ClientPlayerEntity_livingTick;
    }

    @Override
    public void transform(MethodNode main) {
      AbstractInsnNode applySlowdownSpeedNode =
          ASMHelper.findPattern(
              main.instructions.getFirst(),
              new int[]{IFNE, 0x00, 0x00, ALOAD, GETFIELD, DUP, GETFIELD, LDC, FMUL, PUTFIELD},
              "x??xxxxxxx");

      Objects.requireNonNull(
          applySlowdownSpeedNode, "Find pattern failed for applySlowdownSpeedNode");

      // get label it jumps to
      LabelNode jumpTo = ((JumpInsnNode) applySlowdownSpeedNode).label;

      InsnList insnList = new InsnList();
      insnList.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isNoSlowDownActivated));
      insnList.add(new JumpInsnNode(IFNE, jumpTo));

      main.instructions.insert(applySlowdownSpeedNode, insnList);
    }
  }

  @RegisterTransformer
  public static class OnUpdate extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.ClientPlayerEntity_tick;
    }

    @Override
    public void transform(MethodNode main) {
      // AbstractInsnNode top =
      //    ASMHelper.findPattern(main, INVOKESPECIAL, NONE, NONE, ALOAD, INVOKEVIRTUAL, IFEQ);
      AbstractInsnNode top =
          new ASMPattern.Builder()
              .codeOnly()
              .opcodes(INVOKESPECIAL, ALOAD, INVOKEVIRTUAL, IFEQ)
              .build()
              .test(main)
              .getFirst();

      AbstractInsnNode afterRiding = ASMHelper.findPattern(main, GOTO);
      AbstractInsnNode afterWalking =
          ASMHelper.findPattern(main, INVOKESPECIAL, ASMHelper.MagicOpcodes.NONE, ASMHelper.MagicOpcodes.NONE, ASMHelper.MagicOpcodes.NONE, RETURN);
      AbstractInsnNode ret = ASMHelper.findPattern(main, RETURN);

      Objects.requireNonNull(top, "Find pattern failed for top node");
      Objects.requireNonNull(afterRiding, "Find pattern failed for afterRiding node");
      Objects.requireNonNull(afterWalking, "Find pattern failed for afterWalking node");

      LabelNode jmp = new LabelNode();

      InsnList pre = new InsnList();
      pre.add(new VarInsnNode(ALOAD, 0));
      pre.add(
          ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onUpdateWalkingPlayerPre));
      pre.add(new JumpInsnNode(IFNE, jmp));

      InsnList postRiding = new InsnList();
      postRiding.add(new VarInsnNode(ALOAD, 0));
      postRiding.add(
          ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onUpdateWalkingPlayerPost));

      InsnList postWalking = new InsnList();
      postWalking.add(new VarInsnNode(ALOAD, 0));
      postWalking.add(
          ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onUpdateWalkingPlayerPost));

      main.instructions.insert(top, pre);
      main.instructions.insertBefore(afterRiding, postRiding);
      main.instructions.insert(afterWalking, postWalking);
      main.instructions.insertBefore(ret, jmp);
    }
  }

  @RegisterTransformer
  public static class pushOutOfBlocks extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.ClientPlayerEntity_pushOutOfBlocks;
    }

    @Override
    public void transform(MethodNode main) {
      AbstractInsnNode preNode = main.instructions.getFirst();
      AbstractInsnNode postNode =
          ASMHelper.findPattern(main.instructions.getFirst(), new int[]{ICONST_0, IRETURN}, "xx");

      Objects.requireNonNull(preNode, "Find pattern failed for pre node");
      Objects.requireNonNull(postNode, "Find pattern failed for post node");

      LabelNode endJump = new LabelNode();

      InsnList insnPre = new InsnList();
      insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPushOutOfBlocks));
      insnPre.add(new JumpInsnNode(IFNE, endJump));

      main.instructions.insertBefore(preNode, insnPre);
      main.instructions.insertBefore(postNode, endJump);
    }
  }

  @RegisterTransformer
  public static class RowingBoat extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.ClientPlayerEntity_isRowingBoat;
    }

    @Override
    public void transform(MethodNode main) {
      AbstractInsnNode preNode = main.instructions.getFirst();

      Objects.requireNonNull(preNode, "Find pattern failed for pre node");

      LabelNode jump = new LabelNode();

      InsnList insnPre = new InsnList();
      // insnPre.add(ASMHelper.call(GETSTATIC,
      // TypesHook.Fields.ForgeHaxHooks_isNotRowingBoatActivated));
      // insnPre.add(new JumpInsnNode(IFEQ, jump));

      insnPre.add(new InsnNode(ICONST_0));
      insnPre.add(new InsnNode(IRETURN)); // return false
      // insnPre.add(jump);

      main.instructions.insert(insnPre);
    }
  }
}
