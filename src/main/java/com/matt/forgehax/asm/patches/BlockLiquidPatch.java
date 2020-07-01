package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.utils.asmtype.ASMMethod;

import java.util.Objects;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

public class BlockLiquidPatch extends ClassTransformer {

  public BlockLiquidPatch() {
    super(Classes.Liquid);
  }

  @RegisterMethodTransformer
  private class canCollideCheck extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return Methods.Liquid_canCollideCheck;
    }
    
    @Inject(description = "Set collisions with liquids as true")
    public void inject(MethodNode main) {
      AbstractInsnNode node =
        ASMHelper.findPattern(main.instructions.getFirst(),
          new int[]{ ILOAD, IFEQ, ALOAD },
          "xxx");
      
      Objects.requireNonNull(node, "Find pattern failed for node");
      
      InsnList insnList = new InsnList();
      final LabelNode jmp = new LabelNode();
      
      insnList.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_isLiquidInteractEnabled));
      insnList.add(new JumpInsnNode(IFEQ, jmp));
      insnList.add(new InsnNode(ICONST_1));
      insnList.add(new InsnNode(IRETURN));
      insnList.add(jmp);
      
      main.instructions.insertBefore(node, insnList);
    }
  }
}
