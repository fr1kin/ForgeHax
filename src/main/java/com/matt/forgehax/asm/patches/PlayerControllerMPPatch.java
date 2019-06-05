package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.transformer.RegisterTransformer;
import com.matt.forgehax.asm.transformer.Transformer;
import com.matt.forgehax.asm.utils.ASMHelper;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class PlayerControllerMPPatch {

  @RegisterTransformer
  public static class SyncCurrentPlayItem implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<Target> targets() {
      return ASMHelper.getTargetSet(Methods.PlayerControllerMC_syncCurrentPlayItem);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerItemSync));

      main.instructions.insert(list);

      return main;
    }

  }

  @RegisterTransformer
  public static class AttackEntity implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<Target> targets() {
      return ASMHelper.getTargetSet(Methods.PlayerControllerMC_attackEntity);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(new VarInsnNode(ALOAD, 1));
      list.add(new VarInsnNode(ALOAD, 2));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerAttackEntity));

      main.instructions.insert(list);

      return main;
    }

  }

  @RegisterTransformer
  public static class OnPlayerDamageBlock implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<Target> targets() {
      return ASMHelper.getTargetSet(Methods.PlayerControllerMC_onPlayerDamageBlock);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(new VarInsnNode(ALOAD, 1));
      list.add(new VarInsnNode(ALOAD, 2));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerBreakingBlock));

      main.instructions.insert(list);

      return main;
    }

  }

  @RegisterTransformer
  public static class OnStoppedUsingItem implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<Target> targets() {
      return ASMHelper.getTargetSet(Methods.PlayerControllerMC_onStoppedUsingItem);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      AbstractInsnNode last =
          ASMHelper.findPattern(main.instructions.getFirst(), new int[] {RETURN}, "x");

      Objects.requireNonNull(last, "Could not find RET opcode");

      LabelNode label = new LabelNode();

      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, 0));
      list.add(new VarInsnNode(ALOAD, 1));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPlayerStopUse));
      list.add(new JumpInsnNode(IFNE, label));

      main.instructions.insert(list);
      main.instructions.insertBefore(last, label);

      return main;
    }

  }
}
