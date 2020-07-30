package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMField;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.objectweb.asm.tree.*;

@ClassMapping(LivingEntity.class)
public class LivingEntityPatch extends Patch {

  @Inject
  @MethodMapping("travel")
  public void travel(MethodNode node,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "onEntityBlockSlipApply",
          args = {float.class, LivingEntity.class, BlockPos.class},
          ret = float.class
      ) ASMMethod hook) {
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
    list.add(new VarInsnNode(ALOAD, 7)); // block position under
    list.add(ASMHelper.call(INVOKESTATIC, hook));
    // top of stack should be a modified or unmodified slippery float

    //
    node.instructions.insert(first, list); // insert after
  }

  @Inject
  @MethodMapping("travel")
  public void travel_Elytra(MethodNode node,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "shouldApplyElytraMovement",
          args = {boolean.class, LivingEntity.class},
          ret = boolean.class
      ) ASMMethod hook,
      @MethodMapping("isElytraFlying") ASMMethod isElytraFlying) {
    AbstractInsnNode flyingNode = ASMPattern.builder()
        .codeOnly()
        .custom(n -> {
          if (n instanceof MethodInsnNode) {
            return isElytraFlying.isNameEqual(((MethodInsnNode) n).name);
          }
          return false;
        })
        .opcode(IFEQ)
        .find(node)
        .getFirst();

    InsnList list = new InsnList();
    // currently the return value of isElytraFlying is on the top
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(ASMHelper.call(INVOKESTATIC, hook));
    // below this should be an IFEQ opcode

    // insert hook after isElytraFlying call but before IFEQ
    node.instructions.insert(flyingNode, list);
  }

  @Inject
  @MethodMapping("livingTick")
  public void transform(MethodNode node,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "shouldClampMotion",
          args = {LivingEntity.class},
          ret = boolean.class
      ) ASMMethod hook,
      @FieldMapping(
          parentClass = Vector3d.class,
          value = "z"
      ) ASMField vec3d_z) {
    // double d5 = Vector3d.z;
    // >HERE<
    AbstractInsnNode postStore = ASMPattern.builder()
        .custom(n -> {
          if (n instanceof FieldInsnNode && n.getOpcode() == GETFIELD) {
            FieldInsnNode mn = (FieldInsnNode) n;
            return vec3d_z.isNameEqual(mn.name);
          }
          return false;
        })
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
    list.add(ASMHelper.call(INVOKESTATIC, hook));
    list.add(new JumpInsnNode(IFEQ, skipLabel));

    node.instructions.insert(postStore, list);
    node.instructions.insertBefore(beforeMotionCall, skipLabel);
  }
}
