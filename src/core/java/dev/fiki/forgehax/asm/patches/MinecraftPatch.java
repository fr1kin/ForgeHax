package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import org.objectweb.asm.tree.*;

import static dev.fiki.forgehax.asm.TypesMc.Fields.Minecraft_leftClickCounter;

public class MinecraftPatch {
  

  @RegisterTransformer
  public static class RunTick extends MethodTransformer {

    private boolean isLeftClickField(AbstractInsnNode node, int opcode) {
      if(node instanceof FieldInsnNode && node.getOpcode() == opcode) {
        FieldInsnNode fld = (FieldInsnNode) node;
        return Minecraft_leftClickCounter.isNameEqual(fld.name);
      }
      return false;
    }

    private boolean isPutLeftClickField(AbstractInsnNode node) {
      return isLeftClickField(node, PUTFIELD);
    }
    
    @Override
    public ASMMethod getMethod() {
      return Methods.Minecraft_runTick;
    }

    @Override
    public void transform(MethodNode method) {
      // this.leftClickCounter = 10000;
      AbstractInsnNode node = ASMPattern.builder()
          .opcodes(SIPUSH)
          .custom(this::isPutLeftClickField)
          .find(method)
          .getFirst();
      
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onLeftClickCounterSet));
      
      method.instructions.insert(node, list);
    }
  }
  
  @RegisterTransformer
  public static class SendClickBlockToController extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return Methods.Minecraft_sendClickBlockToController;
    }

    @Override
    public void transform(MethodNode method) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(new VarInsnNode(ILOAD, 1));
      list.add(
        ASMHelper.call(
          INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSendClickBlockToController));
      list.add(new VarInsnNode(ISTORE, 1));
      
      method.instructions.insert(list);
    }
  }
}
