package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import java.util.Objects;

import dev.fiki.forgehax.asm.TypesMc;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class EntityRendererPatch {
  
  @RegisterTransformer
  private static class HurtCameraEffect extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.EntityRenderer_hurtCameraEffect;
    }

    @Override
    public void transform(MethodNode main) {
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
