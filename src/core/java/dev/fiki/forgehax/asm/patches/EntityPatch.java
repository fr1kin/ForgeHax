package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import java.util.Objects;

import org.objectweb.asm.tree.*;

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

  //@RegisterTransformer("ForgeHaxHooks.isSafeWalkActivated")
  public static class Move extends MethodTransformer {

    private boolean isInvokeIsSteppingCarefullyCall(AbstractInsnNode node) {
      if(node instanceof MethodInsnNode) {
        MethodInsnNode mn = (MethodInsnNode) node;
        return Methods.Entity_isSteppingCarefully.isNameEqual(mn.name);
      }
      return false;
    }

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.Entity_move;
    }

    @Override
    public void transform(MethodNode main) {
      AbstractInsnNode sneakFlagNode = ASMPattern.builder()
          .find(main)
          .getFirst("could not find call to Entity::isSteppingCarefully");

      AbstractInsnNode _jumpNode = sneakFlagNode.getNext();

      if(!(_jumpNode instanceof JumpInsnNode)) {
        throw new Error("expected node after INVOKEVIRTUAL to be a jump, but got "
            + _jumpNode.getClass().getSimpleName());
      }

      // the original label to the jump
//      LabelNode skipJump = ((JumpInsnNode) _jumpNode).label;
//
//      InsnList insnList = new InsnList();
//      insnList.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isSafeWalkActivated));
//      insnList.add(new JumpInsnNode(IFEQ, skipJump));
//      insnList.add(thatorJump);
//
//      AbstractInsnNode previousNode = sneakFlagNode.getPrevious();
//      main.instructions.remove(sneakFlagNode); // delete IFEQ
//      main.instructions.insert(previousNode, insnList); // insert new instructions
    }
  }

  @RegisterTransformer("ForgeHaxHooks.isBlockFiltered")
  public static class DoBlockCollisions extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.Entity_doBlockCollisions;
    }

    @Override
    public void transform(MethodNode main) {
      AbstractInsnNode preNode =
          ASMHelper.findPattern(
              main.instructions.getFirst(),
              new int[]{
                  ASTORE,
                  0x00,
                  0x00,
                  ALOAD,
                  INVOKEINTERFACE,
                  ALOAD,
                  GETFIELD,
                  ALOAD,
                  ALOAD,
                  ALOAD,
                  INVOKEVIRTUAL
              },
              "x??xxxxxxxx");
      AbstractInsnNode postNode = ASMHelper.findPattern(preNode, new int[]{GOTO}, "x");

      Objects.requireNonNull(preNode, "Find pattern failed for preNode");
      Objects.requireNonNull(postNode, "Find pattern failed for postNode");

      LabelNode endJump = new LabelNode();

      InsnList insnList = new InsnList();
      insnList.add(new VarInsnNode(ALOAD, 0)); // push entity
      insnList.add(new VarInsnNode(ALOAD, 8)); // push block state
      insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_isBlockFiltered));
      insnList.add(new JumpInsnNode(IFNE, endJump));

      main.instructions.insertBefore(postNode, endJump);
      main.instructions.insert(preNode, insnList);
    }
  }
}
