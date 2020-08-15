package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.hooks.XrayHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.ConditionalInject;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.objectweb.asm.tree.*;

import java.util.Objects;

@ClassMapping(value = ChunkRenderDispatcher.ChunkRender.class, innerClassNames = "RebuildTask")
public class ChunkRenderRebuildTaskPatch extends Patch {

  @Inject
  @ConditionalInject("!OptiFine")
  @MethodMapping("compile")
  public void compile(MethodNode node,
      @MethodMapping(
          parentClass = RenderTypeLookup.class,
          value = "canRenderInLayer",
          args = {BlockState.class, RenderType.class},
          ret = boolean.class
      ) ASMMethod canRenderInLayer,
      @MethodMapping(
          parentClass = XrayHooks.class,
          value = "isXrayBlocks",
          args = {},
          ret = boolean.class
      ) ASMMethod isXrayEnabled,
      @MethodMapping(
          parentClass = XrayHooks.class,
          value = "canRenderInLayerOverride",
          args = {BlockState.class, RenderType.class},
          ret = boolean.class
      ) ASMMethod canRenderInLayerOverride) {

    AbstractInsnNode layerCheck = ASMPattern.builder()
        .codeOnly()
        .custom(canRenderInLayer::matchesStaticMethodNode)
        .find(node)
        .getFirst("Could not find method call to " + canRenderInLayer);

    if (!(layerCheck.getNext() instanceof JumpInsnNode)) {
      throw new IllegalStateException("Expected a jump node, got " + layerCheck.getNext().getClass().getSimpleName());
    }

    // jump to skip rendering in this layer
    JumpInsnNode skipRenderLayer = (JumpInsnNode) layerCheck.getNext();
    LabelNode shouldRenderLayer = new LabelNode();
    LabelNode defaultLayerCheck = new LabelNode();

    // aloads for canRenderInLayer call
    VarInsnNode aloadBlockState = (VarInsnNode) layerCheck.getPrevious().getPrevious();
    VarInsnNode aloadRenderType = (VarInsnNode) layerCheck.getPrevious();

    InsnList pre = new InsnList();
    // check if we are rendering a block right now
    pre.add(ASMHelper.call(INVOKESTATIC, isXrayEnabled));
    // if we are not then jump over this code
    pre.add(new JumpInsnNode(IFEQ, defaultLayerCheck));
    // call override method
    pre.add(new VarInsnNode(ALOAD, aloadBlockState.var));
    pre.add(new VarInsnNode(ALOAD, aloadRenderType.var));
    pre.add(ASMHelper.call(INVOKESTATIC, canRenderInLayerOverride));
    // goto return
    pre.add(new JumpInsnNode(IFNE, shouldRenderLayer));
    pre.add(new JumpInsnNode(GOTO, skipRenderLayer.label));
    pre.add(defaultLayerCheck);

    // add our layer override check and logic before the vanilla one
    node.instructions.insertBefore(aloadBlockState, pre);
    // add success jump node
    node.instructions.insert(skipRenderLayer, shouldRenderLayer);
  }

  @Inject
  @ConditionalInject("OptiFine")
  @MethodMapping("compile")
  public void compileOptiFine(MethodNode node,
      @MethodMapping(
          parentClass = XrayHooks.class,
          value = "isXrayBlocks",
          args = {},
          ret = boolean.class
      ) ASMMethod isXrayEnabled,
      @MethodMapping(
          parentClass = XrayHooks.class,
          value = "canRenderInLayerOverride",
          args = {BlockState.class, RenderType.class},
          ret = boolean.class
      ) ASMMethod canRenderInLayerOverride) {

    AbstractInsnNode reflectField = ASMPattern.builder()
        .codeOnly()
        .custom(an -> an.getOpcode() == GETSTATIC
            && an instanceof FieldInsnNode
            && "ForgeRenderTypeLookup_canRenderInLayerBs".equals(((FieldInsnNode) an).name))
        .find(node)
        .getFirst("Could not find method call to ForgeRenderTypeLookup_canRenderInLayerBs");

    JumpInsnNode jumpToRenderLayer = (JumpInsnNode) reflectField.getPrevious().getPrevious().getPrevious();

    JumpInsnNode skipRenderLayer = null;
    for(AbstractInsnNode next = reflectField; next != null; next = next.getNext()) {
      if (next.getOpcode() == GOTO) {
        skipRenderLayer = (JumpInsnNode) next;
        break;
      }
    }

    Objects.requireNonNull(skipRenderLayer, "Could not find GOTO");


    // jump to skip rendering in this layer
    LabelNode defaultLayerCheck = new LabelNode();

    // aloads for canRenderInLayer call

    InsnList pre = new InsnList();
    // check if we are rendering a block right now
    pre.add(ASMHelper.call(INVOKESTATIC, isXrayEnabled));
    // if we are not then jump over this code
    pre.add(new JumpInsnNode(IFEQ, defaultLayerCheck));
    // call override method
    pre.add(new VarInsnNode(ALOAD, 18));
    pre.add(new VarInsnNode(ALOAD, 24));
    pre.add(ASMHelper.call(INVOKESTATIC, canRenderInLayerOverride));
    // goto return
    pre.add(new JumpInsnNode(IFNE, jumpToRenderLayer.label));
    pre.add(new JumpInsnNode(GOTO, skipRenderLayer.label));
    pre.add(defaultLayerCheck);

    // add our layer override check and logic before the vanilla one
    node.instructions.insertBefore(reflectField, pre);
  }
}
