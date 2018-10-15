package com.matt.forgehax.asm.patches;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class PlayerControllerMCPatch extends ClassTransformer {
  public PlayerControllerMCPatch() {
    super(Classes.PlayerControllerMP);
  }

  @RegisterMethodTransformer
  public class SyncCurrentPlayItem extends MethodTransformer {
    @Override
    public ASMMethod getMethod() {
      return Methods.PlayerControllerMC_syncCurrentPlayItem;
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
      return Methods.PlayerControllerMC_attackEntity;
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
      return Methods.PlayerControllerMC_onPlayerDamageBlock;
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
}
