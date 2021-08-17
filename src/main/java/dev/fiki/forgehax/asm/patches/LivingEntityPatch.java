package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.asm.MapClass;
import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.asm.MapMethod;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMField;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import org.objectweb.asm.tree.*;

@MapClass(LivingEntity.class)
public class LivingEntityPatch extends Patch {

  @Inject
  @MapMethod("travel")
  public void travel(MethodNode node,
      @MapMethod(parentClass = ForgeHaxHooks.class, name = "onEntityBlockSlipApply") ASMMethod hook) {
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
  @MapMethod("travel")
  public void travel_Elytra(MethodNode node,
      @MapMethod(parentClass = ForgeHaxHooks.class, name = "shouldApplyElytraMovement") ASMMethod hook,
      @MapMethod("isFallFlying") ASMMethod isFallFlying) {
    AbstractInsnNode flyingNode = ASMPattern.builder()
        .codeOnly()
        .custom(n -> {
          if (n instanceof MethodInsnNode) {
            return isFallFlying.anyNameEqual(((MethodInsnNode) n).name);
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
  @MapMethod("aiStep")
  public void aiStep(MethodNode node,
      @MapMethod(parentClass = ForgeHaxHooks.class, name = "shouldClampMotion") ASMMethod hook,
      @MapField(parentClass = Vector3d.class, value = "z") ASMField vec3d_z) {
    // double d5 = Vector3d.z;
    // >HERE<
    AbstractInsnNode postStore = ASMPattern.builder()
        .custom(n -> {
          if (n instanceof FieldInsnNode && n.getOpcode() == GETFIELD) {
            FieldInsnNode mn = (FieldInsnNode) n;
            return vec3d_z.anyNameEquals(mn.name);
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
