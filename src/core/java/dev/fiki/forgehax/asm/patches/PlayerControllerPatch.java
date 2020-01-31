package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.transforming.ClassTransformer;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import java.util.Objects;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class PlayerControllerPatch extends ClassTransformer {
  
  public PlayerControllerPatch() {
    super(Classes.PlayerController);
  }
  
  @RegisterMethodTransformer
  public class SyncCurrentPlayItem extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.PlayerController_syncCurrentPlayItem;
    }
    
    @Inject(description = "Add callback at top of method")
    public void inject(MethodNode node) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerItemSync));
      
      node.instructions.insert(list);
    }
  }
  
  @RegisterMethodTransformer
  public class AttackEntity extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.PlayerController_attackEntity;
    }
    
    @Inject(description = "Add callback at top of method")
    public void inject(MethodNode node) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(new VarInsnNode(ALOAD, 1));
      list.add(new VarInsnNode(ALOAD, 2));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerAttackEntity));
      
      node.instructions.insert(list);
    }
  }
  
  @RegisterMethodTransformer
  public class OnPlayerDamageBlock extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.PlayerController_onPlayerDamageBlock;
    }
    
    @Inject(description = "Add callback at top of method")
    public void inject(MethodNode node) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(new VarInsnNode(ALOAD, 1));
      list.add(new VarInsnNode(ALOAD, 2));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerBreakingBlock));
      
      node.instructions.insert(list);
    }
  }
  
  @RegisterMethodTransformer
  public class OnStoppedUsingItem extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.PlayerController_onStoppedUsingItem;
    }
    
    @Inject(description = "Add callback at top of method")
    public void inject(MethodNode node) {
      AbstractInsnNode last =
        ASMHelper.findPattern(node.instructions.getFirst(), new int[]{RETURN}, "x");
      
      Objects.requireNonNull(last, "Could not find RET opcode");
      
      LabelNode label = new LabelNode();
      
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(new VarInsnNode(ALOAD, 1));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerStopUse));
      list.add(new JumpInsnNode(IFNE, label));
      
      node.instructions.insert(list);
      node.instructions.insertBefore(last, label);
    }
  }
}
