package dev.fiki.forgehax.asm.patches;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.hooks.XrayHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IModelData;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Random;

@ClassMapping(BlockModelRenderer.class)
public class BlockModelRendererPatch extends Patch {

  @Inject
  @MethodMapping(
      value = "renderModelSmooth",
      args = {IBlockDisplayReader.class, IBakedModel.class, BlockState.class, BlockPos.class, MatrixStack.class,
          IVertexBuilder.class, boolean.class, Random.class, long.class, int.class, IModelData.class},
      ret = boolean.class
  )
  public void renderModelSmooth(MethodNode node,
      @MethodMapping(
          parentClass = XrayHooks.class,
          value = "isXrayBlocks",
          args = {},
          ret = boolean.class
      ) ASMMethod isXrayEnabled,
      @MethodMapping(
          parentClass = XrayHooks.class,
          value = "shouldMakeTransparent",
          args = {BlockState.class},
          ret = boolean.class
      ) ASMMethod shouldMakeTransparent,
      @MethodMapping(
          parentClass = XrayHooks.class,
          value = "blockRenderFinished",
          args = {},
          ret = void.class
      ) ASMMethod blockRenderFinished) {
    AbstractInsnNode ret = ASMPattern.builder()
        .codeOnly()
        .opcode(IRETURN)
        .find(node)
        .getFirst("Could not find IRETURN node");

    LabelNode xrayDisabledJump = new LabelNode();

    InsnList pre = new InsnList();
    pre.add(ASMHelper.call(INVOKESTATIC, isXrayEnabled));
    pre.add(new JumpInsnNode(IFEQ, xrayDisabledJump));

    pre.add(new VarInsnNode(ALOAD, ASMHelper.getLocalVariable(node, "stateIn", null)
        .orElseThrow(() -> new Error("Could not find local variable stateIn")).index)); // block state
    pre.add(ASMHelper.call(INVOKESTATIC, shouldMakeTransparent));
    // if the block isnt transparent, it is an xrayed block
    pre.add(new JumpInsnNode(IFNE, xrayDisabledJump));

    // store FALSE into boolean argument checkSides (we want to render all sides)
    pre.add(new InsnNode(ICONST_0));
    pre.add(new VarInsnNode(ISTORE, ASMHelper.getLocalVariable(node, "checkSides", Type.BOOLEAN_TYPE)
        .orElseThrow(() -> new Error("Could not find local variable checkSides")).index));

    pre.add(xrayDisabledJump);

    node.instructions.insert(pre);
    node.instructions.insertBefore(ret, ASMHelper.call(INVOKESTATIC, blockRenderFinished));
  }

  @Inject
  @MethodMapping(
      value = "renderModelFlat",
      args = {IBlockDisplayReader.class, IBakedModel.class, BlockState.class, BlockPos.class, MatrixStack.class,
          IVertexBuilder.class, boolean.class, Random.class, long.class, int.class, IModelData.class},
      ret = boolean.class
  )
  public void renderModelFlat(MethodNode node,
      @MethodMapping(
          parentClass = XrayHooks.class,
          value = "isXrayBlocks",
          args = {},
          ret = boolean.class
      ) ASMMethod isXrayEnabled,
      @MethodMapping(
          parentClass = XrayHooks.class,
          value = "shouldMakeTransparent",
          args = {BlockState.class},
          ret = boolean.class
      ) ASMMethod shouldMakeTransparent,
      @MethodMapping(
          parentClass = XrayHooks.class,
          value = "blockRenderFinished",
          args = {},
          ret = void.class
      ) ASMMethod blockRenderFinished) {
    renderModelSmooth(node, isXrayEnabled, shouldMakeTransparent, blockRenderFinished);
  }
}
