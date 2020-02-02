package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.InsnPattern;
import dev.fiki.forgehax.asm.utils.transforming.ClassTransformer;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import java.util.Objects;

import org.objectweb.asm.tree.*;

public class MinecraftPatch extends ClassTransformer {
  
  public MinecraftPatch() {
    super(Classes.Minecraft);
  }
  
  @RegisterMethodTransformer
  public class RunTick extends MethodTransformer {

    private boolean isLeftClickField(AbstractInsnNode node, int opcode) {
      if(node instanceof FieldInsnNode && node.getOpcode() == opcode) {
        FieldInsnNode fld = (FieldInsnNode) node;
        return Fields.Minecraft_leftClickCounter.isNameEqual(fld.name);
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
    
    @Inject(value = "ForgeHaxHooks.onLeftClickCounterSet")
    public void injectFirst(MethodNode method) {
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
  
  @RegisterMethodTransformer
  public class SendClickBlockToController extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return Methods.Minecraft_sendClickBlockToController;
    }
    
    @Inject(value = "ForgeHaxHooks::onSendClickBlockToController")
    public void inject(MethodNode method) {
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
