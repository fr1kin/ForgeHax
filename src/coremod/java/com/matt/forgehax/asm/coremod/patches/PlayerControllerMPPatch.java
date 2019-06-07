package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.TypesHook;
import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class PlayerControllerMPPatch {

  @RegisterTransformer
  public static class SyncCurrentPlayItem implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<ITransformer.Target> targets() {
      return ASMHelper.getTargetSet(Methods.PlayerControllerMC_syncCurrentPlayItem);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(Opcodes.ALOAD, 0));
      list.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerItemSync));

      main.instructions.insert(list);

      return main;
    }

  }

  @RegisterTransformer
  public static class AttackEntity implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<ITransformer.Target> targets() {
      return ASMHelper.getTargetSet(Methods.PlayerControllerMC_attackEntity);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(Opcodes.ALOAD, 0));
      list.add(new VarInsnNode(Opcodes.ALOAD, 1));
      list.add(new VarInsnNode(Opcodes.ALOAD, 2));
      list.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerAttackEntity));

      main.instructions.insert(list);

      return main;
    }

  }

  @RegisterTransformer
  public static class OnPlayerDamageBlock implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<ITransformer.Target> targets() {
      return ASMHelper.getTargetSet(Methods.PlayerControllerMC_onPlayerDamageBlock);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(Opcodes.ALOAD, 0));
      list.add(new VarInsnNode(Opcodes.ALOAD, 1));
      list.add(new VarInsnNode(Opcodes.ALOAD, 2));
      list.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerBreakingBlock));

      main.instructions.insert(list);

      return main;
    }

  }

  @RegisterTransformer
  public static class OnStoppedUsingItem implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<ITransformer.Target> targets() {
      return ASMHelper.getTargetSet(Methods.PlayerControllerMC_onStoppedUsingItem);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      AbstractInsnNode last =
          ASMHelper.findPattern(main.instructions.getFirst(), new int[] {Opcodes.RETURN}, "x");

      Objects.requireNonNull(last, "Could not find RET opcode");

      LabelNode label = new LabelNode();

      InsnList list = new InsnList();
      list.add(new VarInsnNode(Opcodes.ALOAD, 0));
      list.add(new VarInsnNode(Opcodes.ALOAD, 1));
      list.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerStopUse));
      list.add(new JumpInsnNode(Opcodes.IFNE, label));

      main.instructions.insert(list);
      main.instructions.insertBefore(last, label);

      return main;
    }

  }
}
