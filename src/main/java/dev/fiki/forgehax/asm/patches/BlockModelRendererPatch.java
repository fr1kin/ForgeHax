package dev.fiki.forgehax.asm.patches;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import dev.fiki.forgehax.api.asm.MapClass;
import dev.fiki.forgehax.api.asm.MapMethod;
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

@MapClass(BlockModelRenderer.class)
public class BlockModelRendererPatch extends Patch {

  @Inject
  @MapMethod(
      name = "renderModelSmooth",
      argTypes = {IBlockDisplayReader.class, IBakedModel.class, BlockState.class, BlockPos.class, MatrixStack.class,
          IVertexBuilder.class, boolean.class, Random.class, long.class, int.class, IModelData.class},
      retType = boolean.class)
  public void renderModelSmooth(MethodNode node,
      @MapMethod(parentClass = XrayHooks.class, name = "isXrayBlocks") ASMMethod isXrayEnabled,
      @MapMethod(parentClass = XrayHooks.class, name = "shouldMakeTransparent") ASMMethod shouldMakeTransparent,
      @MapMethod(parentClass = XrayHooks.class, name = "blockRenderFinished") ASMMethod blockRenderFinished) {
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
  @MapMethod(
      value = "renderModelFlat",
      argTypes = {IBlockDisplayReader.class, IBakedModel.class, BlockState.class, BlockPos.class, MatrixStack.class,
          IVertexBuilder.class, boolean.class, Random.class, long.class, int.class, IModelData.class},
      retType = boolean.class)
  public void renderModelFlat(MethodNode node,
      @MapMethod(parentClass = XrayHooks.class, name = "isXrayBlocks") ASMMethod isXrayEnabled,
      @MapMethod(parentClass = XrayHooks.class, name = "shouldMakeTransparent") ASMMethod shouldMakeTransparent,
      @MapMethod(parentClass = XrayHooks.class, name = "blockRenderFinished") ASMMethod blockRenderFinished) {
    renderModelSmooth(node, isXrayEnabled, shouldMakeTransparent, blockRenderFinished);
  }
}
