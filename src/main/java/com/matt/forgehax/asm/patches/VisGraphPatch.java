package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.transformer.RegisterTransformer;
import com.matt.forgehax.asm.transformer.Transformer;
import com.matt.forgehax.asm.utils.ASMHelper;
import java.util.Objects;
import java.util.Set;

import cpw.mods.modlauncher.api.ITransformerVotingContext;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

public class VisGraphPatch {

  @RegisterTransformer
  public static class SetOpaqueCube implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<Target> targets() {
      return ASMHelper.getTargetSet(Methods.VisGraph_setOpaqueCube);
    }

    //@Inject(description = "Add hook at the end that can override the return value")
    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      AbstractInsnNode top = main.instructions.getFirst();
      AbstractInsnNode bottom =
          ASMHelper.findPattern(main.instructions.getFirst(), new int[] {RETURN}, "x");

      Objects.requireNonNull(top, "Find pattern failed for top");
      Objects.requireNonNull(bottom, "Find pattern failed for bottom");

      LabelNode cancelNode = new LabelNode();

      InsnList insnList = new InsnList();
      insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldDisableCaveCulling));
      insnList.add(new JumpInsnNode(IFNE, cancelNode));

      main.instructions.insertBefore(top, insnList);
      main.instructions.insertBefore(bottom, cancelNode);
      return main;
    }
  }

  @RegisterTransformer
  public static class ComputeVisibility implements Transformer<MethodNode> {
    @Nonnull
    @Override
    public Set<Target> targets() {
      return ASMHelper.getTargetSet(Methods.VisGraph_computeVisibility);
    }

    @Nonnull
    @Override
    public MethodNode transform(MethodNode main, ITransformerVotingContext context) {
      AbstractInsnNode node = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {SIPUSH, IF_ICMPGE}, "xx");

      Objects.requireNonNull(node, "Find pattern failed for node");

      // gets opcode IF_ICMPGE
      JumpInsnNode greaterThanJump = (JumpInsnNode) node.getNext();
      LabelNode nextIfStatement = greaterThanJump.label;
      LabelNode orLabel = new LabelNode();

      // remove IF_ICMPGE
      main.instructions.remove(greaterThanJump);

      InsnList insnList = new InsnList();
      insnList.add(new JumpInsnNode(IF_ICMPLT, orLabel));
      insnList.add(
          ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldDisableCaveCulling));
      insnList.add(new JumpInsnNode(IFEQ, nextIfStatement));
      insnList.add(orLabel);

      main.instructions.insert(node, insnList);
      return main;
    }
  }

}
