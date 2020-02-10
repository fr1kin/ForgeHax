package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import org.objectweb.asm.tree.*;

public class BoatEntityPatch {

  
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
  
  @RegisterTransformer("ForgeHaxHooks.isNoClampingActivated")
  public static class ApplyYawToEntity extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.BoatEntity_applyYawToEntity;
    }

    @Override
    public void transform(MethodNode main) {
      AbstractInsnNode pre = ASMPattern.builder()
          .codeOnly()
          .opcodes(FLOAD, LDC, LDC, INVOKESTATIC, FSTORE)
          .find(main)
          .getFirst("");

      AbstractInsnNode post = pre.getNext().getNext().getNext(); //INVOKESTATIC
      
      InsnList insnList = new InsnList();
      
      LabelNode jump = new LabelNode();
      
      insnList.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isNoClampingActivated));
      insnList.add(new JumpInsnNode(IFNE, jump)); // if nogravity is enabled
      
      main.instructions.insert(pre, insnList);
      main.instructions.insert(post, jump);
    }
  }
}
