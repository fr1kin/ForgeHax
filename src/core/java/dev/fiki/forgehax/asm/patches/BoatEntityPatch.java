package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.InsnPattern;
import dev.fiki.forgehax.asm.utils.transforming.ClassTransformer;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

public class BoatEntityPatch extends ClassTransformer {
  
  public BoatEntityPatch() {
    super(TypesMc.Classes.BoatEntity);
  }
  
  @RegisterMethodTransformer
  private class UpdateMotion extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.BoatEntity_updateMotion;
    }
    
    @Inject(description = "Add hook to disable boat gravity")
    public void inject(MethodNode main) {
      InsnPattern gravity = ASMPattern.builder()
          .codeOnly()
          .opcodes(ALOAD, DUP, GETFIELD, DLOAD, DADD, PUTFIELD)
          .find(main);

      AbstractInsnNode gravityNode = gravity.getFirst("gravity node");
      AbstractInsnNode putFieldNode = gravity.getLast("PUTFIELD node");
      
      LabelNode newLabelNode = new LabelNode();
      
      InsnList insnList = new InsnList();
      insnList.add(
        ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isNoBoatGravityActivated));
      insnList.add(new JumpInsnNode(IFNE, newLabelNode)); // if nogravity is enabled
      
      main.instructions.insertBefore(gravityNode, insnList); // insert if
      main.instructions.insert(putFieldNode, newLabelNode); // end if
    }
  }
  
  @RegisterMethodTransformer
  private class ControlBoat extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.BoatEntity_controlBoat;
    }
    
    @Inject(description = "Add hooks to disable boat rotation")
    public void inject(MethodNode main) {
      InsnPattern first = ASMPattern.builder()
          .codeOnly()
          .opcodes(ALOAD, DUP, GETFIELD, LDC, FADD, PUTFIELD)
          .find(main);

      InsnPattern second = ASMPattern.builder()
          .codeOnly()
          .opcodes(ALOAD, DUP, GETFIELD, FCONST_1, FADD, PUTFIELD)
          .find(main);

      AbstractInsnNode rotationLeftNode = first.getFirst("first node");
      AbstractInsnNode rotationRightNode = second.getFirst("second node");
      
      AbstractInsnNode putFieldNodeLeft = first.getLast(); // get last instruction for left
      AbstractInsnNode putFieldNodeRight = second.getLast(); // get last instruction for right
      
      /*
       * disable updating deltaRotation for strafing left
       */
      LabelNode newLabelNodeLeft = new LabelNode();
      
      InsnList insnListLeft = new InsnList();
      insnListLeft.add(
        ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isBoatSetYawActivated));
      insnListLeft.add(new JumpInsnNode(IFNE, newLabelNodeLeft)); // if nogravity is enabled
      
      main.instructions.insertBefore(rotationLeftNode, insnListLeft); // insert if
      main.instructions.insert(putFieldNodeLeft, newLabelNodeLeft); // end if
      
      /*
       * disable updating deltaRotation for strafing right
       */
      LabelNode newLabelNodeRight = new LabelNode();
      
      InsnList insnListRight = new InsnList();
      insnListRight.add(
        ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isBoatSetYawActivated));
      insnListRight.add(new JumpInsnNode(IFNE, newLabelNodeRight)); // if nogravity is enabled
      
      main.instructions.insertBefore(rotationRightNode, insnListRight); // insert if
      main.instructions.insert(putFieldNodeRight, newLabelNodeRight); // end if
    }
  }
  
  @RegisterMethodTransformer
  private class ApplyYawToEntity extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.BoatEntity_applyYawToEntity;
    }
    
    @Inject(description = "Disable boat view clamping")
    public void inject(MethodNode main) {
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
