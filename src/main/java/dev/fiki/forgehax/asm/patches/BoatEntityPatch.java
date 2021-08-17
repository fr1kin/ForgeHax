package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.asm.MapClass;
import dev.fiki.forgehax.api.asm.MapMethod;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.InsnPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.entity.item.BoatEntity;
import org.objectweb.asm.tree.*;

@MapClass(BoatEntity.class)
public class BoatEntityPatch extends Patch {
//  @RegisterTransformer("ForgeHaxHooks.isBoatSetYawActivated")
//  public static class ControlBoat extends MethodTransformer {
//
//    private boolean isLeftInputDownField(AbstractInsnNode node) {
//      if(node instanceof FieldInsnNode && node.getOpcode() == GETFIELD) {
//        FieldInsnNode fld = (FieldInsnNode) node;
//        return Fields.BoatEntity_leftInputDown.isNameEqual(fld.name);
//      }
//      return false;
//    }
//
//    private boolean isRightInputDownField(AbstractInsnNode node) {
//      if(node instanceof FieldInsnNode && node.getOpcode() == GETFIELD) {
//        FieldInsnNode fld = (FieldInsnNode) node;
//        return Fields.BoatEntity_rightInputDown.isNameEqual(fld.name);
//      }
//      return false;
//    }
//
//    @Override
//    public ASMMethod getMethod() {
//      return TypesMc.Methods.BoatEntity_controlBoat;
//    }
//
//    @Override
//    public void transform(MethodNode main) {
//      InsnPattern leftDownNode = ASMPattern.builder()
//          .codeOnly()
//          .opcodes(ALOAD, GETFIELD, IFEQ, ALOAD, DUP, GETFIELD)
//          //.custom(this::isLeftInputDownField)
//          .find(main);
//
//      InsnPattern second = ASMPattern.builder()
//          .codeOnly()
//          .opcode(ALOAD)
//          .custom(this::isRightInputDownField)
//          .find(main);
//
//      AbstractInsnNode rotationLeftNode = leftDownNode.getFirst("leftInputDown node");
//      AbstractInsnNode rotationRightNode = second.getFirst("rightInputDown node");
//
//      AbstractInsnNode putFieldNodeLeft = leftDownNode.getLast(); // get last instruction for left
//      AbstractInsnNode putFieldNodeRight = second.getLast(); // get last instruction for right
//
//      /*
//       * disable updating deltaRotation for strafing left
//       */
//      LabelNode newLabelNodeLeft = new LabelNode();
//
//      InsnList insnListLeft = new InsnList();
//      insnListLeft.add(
//        ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isBoatSetYawActivated));
//      insnListLeft.add(new JumpInsnNode(IFNE, newLabelNodeLeft)); // if nogravity is enabled
//
//      main.instructions.insertBefore(rotationLeftNode, insnListLeft); // insert if
//      main.instructions.insert(putFieldNodeLeft, newLabelNodeLeft); // end if
//
//      /*
//       * disable updating deltaRotation for strafing right
//       */
//      LabelNode newLabelNodeRight = new LabelNode();
//
//      InsnList insnListRight = new InsnList();
//      insnListRight.add(
//        ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isBoatSetYawActivated));
//      insnListRight.add(new JumpInsnNode(IFNE, newLabelNodeRight)); // if nogravity is enabled
//
//      main.instructions.insertBefore(rotationRightNode, insnListRight); // insert if
//      main.instructions.insert(putFieldNodeRight, newLabelNodeRight); // end if
//    }
//  }

  @Inject
  @MapMethod("clampRotation")
  public void clampRotation(MethodNode main,
      @MapMethod(parentClass = ForgeHaxHooks.class, name = "shouldClampBoat") ASMMethod hook) {
    InsnPattern nodes = ASMPattern.builder()
        .codeOnly()
        .opcodes(FLOAD, LDC, LDC, INVOKESTATIC, FSTORE)
        .find(main);

    AbstractInsnNode pre = nodes.getFirst();
    AbstractInsnNode post = nodes.getLast(); // FSTORE

    LabelNode jump = new LabelNode();

    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(ASMHelper.call(INVOKESTATIC, hook));
    list.add(new JumpInsnNode(IFEQ, jump));

    main.instructions.insert(pre, list);
    main.instructions.insertBefore(post, jump);
  }
}
