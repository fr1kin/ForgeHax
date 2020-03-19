package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.common.asmtype.ASMMethod;
import org.objectweb.asm.tree.*;

/**
 * Created on 11/13/2016 by fr1kin
 */
public class ClientEntityPlayerPatch {

  @RegisterTransformer("ForgeHaxHooks::shouldSlowdownPlayer")
  public static class ApplyLivingUpdate extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.ClientPlayerEntity_livingTick;
    }

    @Override
    public void transform(MethodNode main) {
      AbstractInsnNode skipNode = ASMPattern.builder()
          .codeOnly()
          .opcodes(ALOAD, INVOKEVIRTUAL, IFEQ, ALOAD, INVOKEVIRTUAL, IFNE)
          .find(main)
          .getLast("could not find IFNE node");

      LabelNode skip = ((JumpInsnNode) skipNode).label;

      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldSlowdownPlayer));
      list.add(new JumpInsnNode(IFEQ, skip));

      main.instructions.insert(skipNode, list);
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

  @RegisterTransformer("ForgeHaxHooks::shouldNotRowBoat")
  public static class RowingBoat extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.ClientPlayerEntity_isRowingBoat;
    }

    @Override
    public void transform(MethodNode main) {
      AbstractInsnNode ret = ASMPattern.builder()
          .codeOnly()
          .opcode(IRETURN)
          .find(main)
          .getFirst("could not find return node");

      LabelNode end = new LabelNode();
      LabelNode jump = new LabelNode();

      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldNotRowBoat));
      list.add(new JumpInsnNode(IFEQ, jump));
      list.add(new InsnNode(ICONST_0));
      list.add(new JumpInsnNode(GOTO, end));
      list.add(jump);

      main.instructions.insert(list);
      main.instructions.insertBefore(ret, end);
    }
  }
}
