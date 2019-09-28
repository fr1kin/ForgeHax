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
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class EntityLivingBasePatch extends ClassTransformer {
  
  public EntityLivingBasePatch() {
    super(Classes.EntityLivingBase);
  }
  
  @RegisterMethodTransformer
  public class Travel extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return Methods.EntityLivingBase_travel;
    }
    
    @Inject(description = "Add hook before first slippery motion calculation")
    public void injectFirst(MethodNode node) {
      // at first underState.getBlock().getSlipperiness(...)
      AbstractInsnNode first =
        ASMHelper.findPattern(
          node,
          INVOKEVIRTUAL,
          LDC,
          FMUL,
          FSTORE,
          NONE,
          NONE,
          NONE,
          LDC,
          FLOAD,
          FLOAD,
          FMUL,
          FLOAD,
          FMUL,
          FDIV,
          FSTORE,
          NONE,
          NONE,
          ALOAD,
          GETFIELD,
          IFEQ);
      
      Objects.requireNonNull(first, "Could not find first slip motion node");
      
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(new VarInsnNode(ALOAD, 6));
      list.add(new InsnNode(ICONST_0));
      list.add(
        ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onEntityBlockSlipApply));
      // top of stack should be a modified or unmodified slippery float
      
      node.instructions.insert(first, list); // insert after
    }
    
    @Inject(description = "Add hook before second slippery motion calculation")
    public void injectSecond(MethodNode node) {
      // at second underState.getBlock().getSlipperiness(...)
      AbstractInsnNode second =
        ASMHelper.findPattern(
          node,
          INVOKEVIRTUAL,
          LDC,
          FMUL,
          FSTORE,
          NONE,
          NONE,
          NONE,
          ALOAD,
          INVOKEVIRTUAL,
          IFEQ,
          NONE,
          NONE,
          LDC,
          FSTORE,
          NONE,
          NONE,
          ALOAD,
          ALOAD,
          GETFIELD,
          LDC,
          LDC,
          INVOKESTATIC,
          PUTFIELD);
      
      Objects.requireNonNull(second, "Could not find second slip motion node");
      
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(new VarInsnNode(ALOAD, 8));
      list.add(new InsnNode(ICONST_1));
      list.add(
        ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onEntityBlockSlipApply));
      // top of stack should be a modified or unmodified slippery float
      
      node.instructions.insert(second, list); // insert after
    }
  }
}
