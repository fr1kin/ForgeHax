package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import java.util.Objects;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class EntityRendererPatch extends ClassTransformer {
  
  public EntityRendererPatch() {
    super(Classes.EntityRenderer);
  }
  
  @RegisterMethodTransformer
  private class HurtCameraEffect extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return Methods.EntityRenderer_hurtCameraEffect;
    }
    
    @Inject(description = "Add hook that allows the method to be canceled")
    public void inject(MethodNode main) {
      AbstractInsnNode preNode = main.instructions.getFirst();
      AbstractInsnNode postNode =
        ASMHelper.findPattern(main.instructions.getFirst(), new int[]{RETURN}, "x");
      
      Objects.requireNonNull(preNode, "Find pattern failed for preNode");
      Objects.requireNonNull(postNode, "Find pattern failed for postNode");
      
      LabelNode endJump = new LabelNode();
      
      InsnList insnPre = new InsnList();
      insnPre.add(new VarInsnNode(FLOAD, 1));
      insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onHurtcamEffect));
      insnPre.add(new JumpInsnNode(IFNE, endJump));
      
      main.instructions.insertBefore(preNode, insnPre);
      main.instructions.insertBefore(postNode, endJump);
    }
  }
}
