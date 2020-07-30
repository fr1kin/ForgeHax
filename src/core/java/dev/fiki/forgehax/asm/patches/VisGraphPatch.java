package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.client.renderer.chunk.VisGraph;
import org.objectweb.asm.tree.*;

import java.util.Objects;

@ClassMapping(VisGraph.class)
public class VisGraphPatch extends Patch {

  @Inject
  @MethodMapping("setOpaqueCube")
  public void setOpaqueCube(MethodNode main,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "shouldDisableCaveCulling",
          args = {},
          ret = boolean.class
      ) ASMMethod hook) {
    AbstractInsnNode top = main.instructions.getFirst();
    AbstractInsnNode bottom = ASMHelper.findPattern(main.instructions.getFirst(), new int[]{RETURN}, "x");

    Objects.requireNonNull(top, "Find pattern failed for top");
    Objects.requireNonNull(bottom, "Find pattern failed for bottom");

    LabelNode cancelNode = new LabelNode();

    InsnList insnList = new InsnList();
    insnList.add(ASMHelper.call(INVOKESTATIC, hook));
    insnList.add(new JumpInsnNode(IFNE, cancelNode));

    main.instructions.insertBefore(top, insnList);
    main.instructions.insertBefore(bottom, cancelNode);
  }

  @Inject
  @MethodMapping("computeVisibility")
  public void computeVisibility(MethodNode main,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "shouldDisableCaveCulling",
          args = {},
          ret = boolean.class
      ) ASMMethod hook) {
    AbstractInsnNode node = ASMHelper.findPattern(main.instructions.getFirst(), new int[]{SIPUSH, IF_ICMPGE}, "xx");

    Objects.requireNonNull(node, "Find pattern failed for node");

    // gets opcode IF_ICMPGE
    JumpInsnNode greaterThanJump = (JumpInsnNode) node.getNext();
    LabelNode nextIfStatement = greaterThanJump.label;
    LabelNode orLabel = new LabelNode();

    // remove IF_ICMPGE
    main.instructions.remove(greaterThanJump);

    InsnList insnList = new InsnList();
    insnList.add(new JumpInsnNode(IF_ICMPLT, orLabel));
    insnList.add(ASMHelper.call(INVOKESTATIC, hook));
    insnList.add(new JumpInsnNode(IFEQ, nextIfStatement));
    insnList.add(orLabel);

    main.instructions.insert(node, insnList);
  }
}
