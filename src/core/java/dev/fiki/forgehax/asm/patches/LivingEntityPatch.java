package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.transforming.ClassTransformer;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class LivingEntityPatch extends ClassTransformer {
  
  public LivingEntityPatch() {
    super(TypesMc.Classes.LivingEntity);
  }
  
  @RegisterMethodTransformer
  public class Travel extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.LivingEntity_travel;
    }
    
    @Inject(value = "ForgeHaxHooks::onEntityBlockSlipApply")
    public void injectFirst(MethodNode node) {
      AbstractInsnNode first = ASMPattern.builder()
          .codeOnly()
          // float f5 = this.world.getBlockState(....
          .opcodes(INVOKEVIRTUAL, FSTORE)
          // float f7 = this.onGround ? f5....
          .opcodes(ALOAD, GETFIELD, IFEQ, FLOAD, LDC, FMUL, GOTO)
          .find(node)
          .getFirst();

      InsnList list = new InsnList();
      // slipperiness is on the stack right now
      list.add(new VarInsnNode(ALOAD, 0)); // living entity
      list.add(new VarInsnNode(ALOAD, 6)); // block position under
      list.add(ASMHelper.call(
          INVOKESTATIC,
          TypesHook.Methods.ForgeHaxHooks_onEntityBlockSlipApply
      ));
      // top of stack should be a modified or unmodified slippery float

      //
      node.instructions.insert(first, list); // insert after
    }
  }
}
