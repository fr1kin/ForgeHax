package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import java.util.Objects;

import dev.fiki.forgehax.asm.TypesMc;
import org.objectweb.asm.tree.*;

/**
 * Created on 11/13/2016 by fr1kin
 */
public class ClientEntityPlayerPatch {

  @RegisterTransformer("ForgeHaxHooks.isNoSlowDownActivated")
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

  @RegisterTransformer("ForgeHaxHooks::onUpdateWalkingPlayer(Pre|Post)")
  public static class Tick extends MethodTransformer {

    private boolean isOnWalkingUpdateCall(AbstractInsnNode node) {
      if(node instanceof MethodInsnNode) {
        MethodInsnNode mn = (MethodInsnNode) node;
        return Methods.ClientPlayerEntity_onUpdateWalkingPlayer.isNameEqual(mn.name);
      }
      return false;
    }

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.ClientPlayerEntity_tick;
    }

    @Override
    public void transform(MethodNode main) {
      // <pre>
      // this.onUpdateWalkingPlayer();
      // <post>
      // skip label
      AbstractInsnNode walkingUpdateCall = ASMPattern.builder()
          .custom(this::isOnWalkingUpdateCall)
          .find(main)
          .getFirst("could not find node to onUpdateWalkingPlayer");

      LabelNode jmp = new LabelNode();

      InsnList pre = new InsnList();
      pre.add(new VarInsnNode(ALOAD, 0)); // this*
      pre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onUpdateWalkingPlayerPre));
      pre.add(new JumpInsnNode(IFNE, jmp));

      InsnList post = new InsnList();
      post.add(new VarInsnNode(ALOAD, 0)); // this*
      post.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onUpdateWalkingPlayerPost));
      post.add(jmp);

      // insert above ALOAD
      main.instructions.insertBefore(walkingUpdateCall.getPrevious(), pre);
      // insert below call
      main.instructions.insert(walkingUpdateCall, post);
    }
  }

  @RegisterTransformer
  public static class PushOutOfBlocks extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.ClientPlayerEntity_pushOutOfBlocks;
    }

    @Override
    public void transform(MethodNode main) {
      AbstractInsnNode preNode = main.instructions.getFirst();
      AbstractInsnNode postNode = ASMPattern.builder()
          .codeOnly()
          .opcode(RETURN)
          .find(main)
          .getFirst();

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

  @RegisterTransformer("ClientPlayerEntity.isRowingBoat")
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
