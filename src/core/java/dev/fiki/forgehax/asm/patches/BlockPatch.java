package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.hooks.XrayHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.objectweb.asm.tree.*;

@ClassMapping(Block.class)
public class BlockPatch extends Patch {

//  @Inject
  @MethodMapping("shouldSideBeRendered")
  public void shouldSideBeRendered(MethodNode node,
      @MethodMapping(
          parentClass = IBlockReader.class,
          value = "getBlockState"
      ) ASMMethod getBlockState,
      @MethodMapping(
          parentClass = XrayHooks.class,
          value = "isXrayBlocks",
          args = {},
          ret = boolean.class
      ) ASMMethod isXrayEnabled,
      @MethodMapping(
          parentClass = XrayHooks.class,
          value = "shouldSideBeRendered",
          args = {IBlockReader.class, BlockPos.class, BlockState.class},
          ret = boolean.class
      ) ASMMethod shouldSideBeRenderedOverride) {
    // BlockPos blockpos = blockAccess.offset(pos);
    // BlockState blockstate = blockState.getBlockState(blockpos);
    // >>>HERE<<<
    // ...

    AbstractInsnNode invokeGetBlockState = ASMPattern.builder()
        .codeOnly()
        .custom(n -> getBlockState.matchesInvoke(INVOKEINTERFACE, n))
        .find(node)
        .getFirst("Could not find call to getBlockState");

    VarInsnNode astoreBlockState = (VarInsnNode) invokeGetBlockState.getNext();

    LabelNode skipOverride = new LabelNode();

    InsnList list = new InsnList();
    list.add(ASMHelper.call(INVOKESTATIC, isXrayEnabled));
    list.add(new JumpInsnNode(IFEQ, skipOverride));
    list.add(new VarInsnNode(ALOAD, 1));
    list.add(new VarInsnNode(ALOAD, 2));
    list.add(new VarInsnNode(ALOAD, astoreBlockState.var));
    list.add(ASMHelper.call(INVOKESTATIC, shouldSideBeRenderedOverride));
    list.add(new JumpInsnNode(IFEQ, skipOverride));
    list.add(new InsnNode(ICONST_1));
    list.add(new InsnNode(IRETURN));
    list.add(skipOverride);

    node.instructions.insert(astoreBlockState, list);
  }
}
