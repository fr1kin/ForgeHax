package com.matt.forgehax.asm.coremod.patches;

import com.matt.forgehax.asm.coremod.TypesHook;
import com.matt.forgehax.asm.coremod.transformer.RegisterTransformer;
import com.matt.forgehax.asm.coremod.transformer.Transformer;
import com.matt.forgehax.asm.coremod.utils.ASMHelper;
import java.util.Objects;
import java.util.Set;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

public class VisGraphPatch {

  @RegisterTransformer
  public static class SetOpaqueCube implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<ITransformer.Target> targets() {
      return ASMHelper.getTargetSet(Methods.VisGraph_setOpaqueCube);
    }

    //@Inject(description = "Add hook at the end that can override the return value")
    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      AbstractInsnNode top = main.instructions.getFirst();
      AbstractInsnNode bottom =
          ASMHelper.findPattern(main.instructions.getFirst(), new int[] {Opcodes.RETURN}, "x");

      Objects.requireNonNull(top, "Find pattern failed for top");
      Objects.requireNonNull(bottom, "Find pattern failed for bottom");

      LabelNode cancelNode = new LabelNode();

      InsnList insnList = new InsnList();
      insnList.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldDisableCaveCulling));
      insnList.add(new JumpInsnNode(Opcodes.IFNE, cancelNode));

      main.instructions.insertBefore(top, insnList);
      main.instructions.insertBefore(bottom, cancelNode);
      return main;
    }
  }

  @RegisterTransformer
  public static class ComputeVisibility implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<ITransformer.Target> targets() {
      return ASMHelper.getTargetSet(Methods.VisGraph_computeVisibility);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      AbstractInsnNode node = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {Opcodes.SIPUSH, Opcodes.IF_ICMPGE}, "xx");

      Objects.requireNonNull(node, "Find pattern failed for node");

      // gets opcode IF_ICMPGE
      JumpInsnNode greaterThanJump = (JumpInsnNode) node.getNext();
      LabelNode nextIfStatement = greaterThanJump.label;
      LabelNode orLabel = new LabelNode();

      // remove IF_ICMPGE
      main.instructions.remove(greaterThanJump);

      InsnList insnList = new InsnList();
      insnList.add(new JumpInsnNode(Opcodes.IF_ICMPLT, orLabel));
      insnList.add(
          ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldDisableCaveCulling));
      insnList.add(new JumpInsnNode(Opcodes.IFEQ, nextIfStatement));
      insnList.add(orLabel);

      main.instructions.insert(node, insnList);
      return main;
    }
  }

}
