package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.common.asmtype.ASMMethod;
import org.objectweb.asm.tree.*;

public class LivingEntityPatch {

  @RegisterTransformer("ForgeHaxHooks::onEntityBlockSlipApply")
  public static class Travel_BlockSlip extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return Methods.LivingEntity_travel;
    }

    @Override
    public void transform(MethodNode node) {
      AbstractInsnNode first = ASMPattern.builder()
          .codeOnly()
          // float f5 = this.world.getBlockState(....
          .opcodes(INVOKEVIRTUAL, FSTORE)
          // float f7 = this.onGround ? f5....
          .opcodes(ALOAD, GETFIELD, IFEQ, FLOAD, LDC, FMUL, GOTO)
          .find(node)
          .getFirst();

      InsnList list = new InsnList();
      // slipperiness is on the stack right now
      list.add(new VarInsnNode(ALOAD, 0)); // living entity
      list.add(new VarInsnNode(ALOAD, 6)); // block position under
      list.add(ASMHelper.call(
          INVOKESTATIC,
          TypesHook.Methods.ForgeHaxHooks_onEntityBlockSlipApply
      ));
      // top of stack should be a modified or unmodified slippery float

      //
      node.instructions.insert(first, list); // insert after
    }
  }

  @RegisterTransformer("ForgeHaxHooks::shouldApplyElytraMovement")
  public static class Travel_ElytraMovement extends MethodTransformer {
    private boolean isElytraFlyingCall(AbstractInsnNode node) {
      if(node instanceof MethodInsnNode && node.getOpcode() == INVOKEVIRTUAL) {
        MethodInsnNode mn = (MethodInsnNode) node;
        return Methods.LivingEntity_isElytraFlying.isNameEqual(mn.name);
      }
      return false;
    }

    @Override
    public ASMMethod getMethod() {
      return Methods.LivingEntity_travel;
    }

    @Override
    public void transform(MethodNode node) {
      AbstractInsnNode flyingNode = ASMPattern.builder()
          .codeOnly()
          .custom(this::isElytraFlyingCall)
          .opcode(IFEQ)
          .find(node)
          .getFirst();

      InsnList list = new InsnList();
      // currently the return value of isElytraFlying is on the top
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldApplyElytraMovement));
      // below this should be an IFEQ opcode

      // insert hook after isElytraFlying call but before IFEQ
      node.instructions.insert(flyingNode, list);
    }
  }

  @RegisterTransformer("ForgeHaxHooks::shouldClampMotion")
  public static class LivingTick extends MethodTransformer {
    private boolean isVector3dGetZField(AbstractInsnNode node) {
      if(node instanceof FieldInsnNode && node.getOpcode() == GETFIELD) {
        FieldInsnNode mn = (FieldInsnNode) node;
        return Fields.Vector3d_z.isNameEqual(mn.name);
      }
      return false;
    }

    @Override
    public ASMMethod getMethod() {
      return Methods.LivingEntity_livingTick;
    }

    @Override
    public void transform(MethodNode node) {
      // double d5 = Vector3d.z;
      // >HERE<
      AbstractInsnNode postStore = ASMPattern.builder()
          .custom(this::isVector3dGetZField)
          .opcode(DSTORE)
          .find(node)
          .getLast("Cannot find GETFIELD to Vector3d.z");

      // >HERE<
      // this.setMotion(d1, d3, d5);
      AbstractInsnNode beforeMotionCall = ASMPattern.builder()
          .codeOnly()
          .opcodes(ALOAD, DLOAD, DLOAD, DLOAD)
          .opcode(INVOKEVIRTUAL)
          .find(postStore)
          .getFirst("Cannot find call to ::setMotion");

      LabelNode skipLabel = new LabelNode();

      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldClampMotion));
      list.add(new JumpInsnNode(IFEQ, skipLabel));

      node.instructions.insert(postStore, list);
      node.instructions.insertBefore(beforeMotionCall, skipLabel);
    }
  }
}
