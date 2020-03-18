package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.common.asmtype.ASMMethod;
import org.objectweb.asm.tree.*;

import java.util.Objects;

public class EntityPatch {

  @RegisterTransformer("ForgeHaxHooks::onApplyCollisionMotion")
  public static class ApplyEntityCollision extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.Entity_applyEntityCollision;
    }

    @Override
    public void transform(MethodNode main) {
      // @ this.addVelocity(-d0, 0.0D, -d1);
      AbstractInsnNode thisEntityPreNode =
          ASMHelper.findPattern(
              main.instructions.getFirst(),
              new int[]{ALOAD, DLOAD, DNEG, DCONST_0, DLOAD, DNEG, INVOKEVIRTUAL},
              "xxxxxxx");
      // start at preNode, and scan for next INVOKEVIRTUAL sig
      AbstractInsnNode thisEntityPostNode =
          ASMHelper.findPattern(thisEntityPreNode, new int[]{INVOKEVIRTUAL}, "x");
      // @ entityIn.addVelocity(d0, 0.0D, d1);
      AbstractInsnNode otherEntityPreNode =
          ASMHelper.findPattern(
              thisEntityPostNode,
              new int[]{ALOAD, DLOAD, DCONST_0, DLOAD, INVOKEVIRTUAL},
              "xxxxx");
      // start at preNode, and scan for next INVOKEVIRTUAL sig
      AbstractInsnNode otherEntityPostNode =
          ASMHelper.findPattern(otherEntityPreNode, new int[]{INVOKEVIRTUAL}, "x");

      Objects.requireNonNull(thisEntityPreNode, "Find pattern failed for thisEntityPreNode");
      Objects.requireNonNull(thisEntityPostNode, "Find pattern failed for thisEntityPostNode");
      Objects.requireNonNull(otherEntityPreNode, "Find pattern failed for otherEntityPreNode");
      Objects.requireNonNull(otherEntityPostNode, "Find pattern failed for otherEntityPostNode");

      LabelNode endJumpForThis = new LabelNode();
      LabelNode endJumpForOther = new LabelNode();

      // first we handle this.addVelocity

      InsnList insnThisPre = new InsnList();
      insnThisPre.add(new VarInsnNode(ALOAD, 0)); // push THIS
      insnThisPre.add(new VarInsnNode(ALOAD, 1));
      insnThisPre.add(new VarInsnNode(DLOAD, 2));
      insnThisPre.add(new InsnNode(DNEG)); // push -X
      insnThisPre.add(new VarInsnNode(DLOAD, 4));
      insnThisPre.add(new InsnNode(DNEG)); // push -Z
      insnThisPre.add(
          ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onApplyCollisionMotion));
      insnThisPre.add(new JumpInsnNode(IFNE, endJumpForThis));

      InsnList insnOtherPre = new InsnList();
      insnOtherPre.add(new VarInsnNode(ALOAD, 1)); // push entityIn
      insnOtherPre.add(new VarInsnNode(ALOAD, 0)); // push THIS
      insnOtherPre.add(new VarInsnNode(DLOAD, 2)); // push X
      insnOtherPre.add(new VarInsnNode(DLOAD, 4)); // push Z
      insnOtherPre.add(
          ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onApplyCollisionMotion));
      insnOtherPre.add(new JumpInsnNode(IFNE, endJumpForOther));

      main.instructions.insertBefore(thisEntityPreNode, insnThisPre);
      main.instructions.insert(thisEntityPostNode, endJumpForThis);

      main.instructions.insertBefore(otherEntityPreNode, insnOtherPre);
      main.instructions.insert(otherEntityPostNode, endJumpForOther);
    }
  }

  @RegisterTransformer("ForgeHaxHooks::shouldApplyBlockEntityCollisions")
  public static class DoBlockCollisions extends MethodTransformer {
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.Entity_doBlockCollisions;
    }

    @Override
    public void transform(MethodNode main) {
      // >HERE<
      // blockstate.onEntityCollision(this.world, ...
      AbstractInsnNode pre = ASMPattern.builder()
          .codeOnly()
          .opcodes(ALOAD, ALOAD, GETFIELD, ALOAD, ALOAD, INVOKEVIRTUAL)
          .find(main)
          .getFirst("could not find node to BlockState::onEntityCollision call");

      // this.onInsideBlock(blockstate);
      // >HERE<
      AbstractInsnNode post = ASMPattern.builder()
          .codeOnly()
          .opcode(GOTO)
          .find(pre)
          .getFirst("could not find GOTO label in try block");

      LabelNode skip = new LabelNode();

      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0)); // push entity
      list.add(new VarInsnNode(ALOAD, 11)); // push block state
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldApplyBlockEntityCollisions));
      list.add(new JumpInsnNode(IFEQ, skip)); // skip if return value is equal to 0

      main.instructions.insertBefore(pre, list);
      main.instructions.insertBefore(post, skip);
    }
  }
}
