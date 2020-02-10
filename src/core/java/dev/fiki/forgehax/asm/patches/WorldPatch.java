package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import java.util.Objects;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class WorldPatch {
  

  @RegisterTransformer
  private static class HandleMaterialAcceleration extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.World_handleMaterialAcceleration;
    }

    @Override
    public void transform(MethodNode method) {
      AbstractInsnNode preNode =
        ASMHelper.findPattern(
          method.instructions.getFirst(),
          new int[]{
            ALOAD,
            INVOKEVIRTUAL,
            ASTORE,
            0x00,
            0x00,
            LDC,
            DSTORE,
            0x00,
            0x00,
            ALOAD,
            DUP,
            GETFIELD,
            ALOAD,
            GETFIELD,
            LDC,
            DMUL,
            DADD,
            PUTFIELD
          },
          "xxx??xx??xxxxxxxxx");
      AbstractInsnNode postNode =
        ASMHelper.findPattern(method.instructions.getFirst(), new int[]{ILOAD, IRETURN}, "xx");
      
      Objects.requireNonNull(preNode, "Find pattern failed for preNode");
      Objects.requireNonNull(postNode, "Find pattern failed for postNode");
      
      LabelNode endJump = new LabelNode();
      
      InsnList insnPre = new InsnList();
      insnPre.add(new VarInsnNode(ALOAD, 3));
      insnPre.add(new VarInsnNode(ALOAD, 11));
      insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onWaterMovement));
      insnPre.add(new JumpInsnNode(IFNE, endJump));
      
      method.instructions.insertBefore(preNode, insnPre);
      method.instructions.insertBefore(postNode, endJump);
    }
  }
}
