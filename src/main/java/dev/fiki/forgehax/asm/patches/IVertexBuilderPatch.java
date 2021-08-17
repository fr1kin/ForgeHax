package dev.fiki.forgehax.asm.patches;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import dev.fiki.forgehax.api.asm.MapClass;
import dev.fiki.forgehax.api.asm.MapMethod;
import dev.fiki.forgehax.asm.hooks.XrayHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.ConditionalInject;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.client.renderer.model.BakedQuad;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

@MapClass(IVertexBuilder.class)
public class IVertexBuilderPatch extends Patch {
  @Inject
  @ConditionalInject("!OptiFine")
  @MapMethod(
      name = "putBulkData",
      argTypes = {MatrixStack.Entry.class, BakedQuad.class, float[].class, float.class, float.class, float.class,
          int[].class, int.class, boolean.class},
      retType = void.class
  )
  public void putBulkData(MethodNode node,
      @MapMethod(name = "vertex", argTypes = {float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class, int.class, float.class, float.class, float.class}) ASMMethod addVertex,
      @MapMethod(parentClass = XrayHooks.class, name = "isXrayBlocks") ASMMethod isXrayEnabled,
      @MapMethod(parentClass = XrayHooks.class, name = "changeBrightness") ASMMethod changeBrightness,
      @MapMethod(parentClass = XrayHooks.class, name = "getBlockAlphaOverride") ASMMethod getBlockAlphaOverride) {
    LocalVariableNode lvLights = ASMHelper.getLocalVariable(node, "p_227890_7_", Type.getType(int[].class))
        .orElseThrow(() -> new Error("Could not find local variable combinedLightsIn"));
    LocalVariableNode lvColors = ASMHelper.getLocalVariable(node, "p_227890_3_", Type.getType(float[].class))
        .orElseThrow(() -> new Error("Could not find local variable colorMuls"));

    int alphaIndex = ASMHelper.addNewLocalVariable(node, "alphaValue", Type.FLOAT_TYPE.getDescriptor());

    LabelNode setAlphaToDefaultJump = new LabelNode();
    LabelNode end = new LabelNode();

    InsnList list = new InsnList();

    // only run if xray is enabled
    list.add(ASMHelper.call(INVOKESTATIC, isXrayEnabled));
    list.add(new JumpInsnNode(IFEQ, setAlphaToDefaultJump));

    // set the brightness to full bright if this is an xray block
    // if its a transparent block, we will continue and set the alpha
    // to our desired value
    list.add(new VarInsnNode(ALOAD, lvLights.index));
    list.add(new VarInsnNode(ALOAD, lvColors.index));
    list.add(ASMHelper.call(INVOKESTATIC, changeBrightness));
    list.add(new JumpInsnNode(IFEQ, setAlphaToDefaultJump));

    list.add(ASMHelper.call(INVOKESTATIC, getBlockAlphaOverride));
    list.add(new VarInsnNode(FSTORE, alphaIndex));

    list.add(new JumpInsnNode(GOTO, end));

    list.add(setAlphaToDefaultJump);

    list.add(new InsnNode(FCONST_1));
    list.add(new VarInsnNode(FSTORE, alphaIndex));

    list.add(end);

    AbstractInsnNode vertexCall = ASMPattern.builder()
        .custom(an -> addVertex.matchesInvoke(INVOKEINTERFACE, an))
        .find(node)
        .getFirst("Could not find call to addVertex!");

    // find the opcode we want to replace with FLOAD
    AbstractInsnNode target = vertexCall.getPrevious().getPrevious().getPrevious().getPrevious().getPrevious()
        .getPrevious().getPrevious().getPrevious().getPrevious().getPrevious().getPrevious();

    // add FLOAD and remove FCONST_1
    node.instructions.insertBefore(target, new VarInsnNode(FLOAD, alphaIndex));
    node.instructions.remove(target);

    node.instructions.insert(list);
  }

  @Inject
  @ConditionalInject("OptiFine")
  @MapMethod(
      name = "addQuad",
      argTypes = {MatrixStack.Entry.class, BakedQuad.class, float[].class,
          float.class, float.class, float.class, float.class,
          int[].class, int.class, boolean.class},
      retType = void.class,
      allowInvalid = true
  )
  public void addQuadOptiFine(MethodNode node,
      @MapMethod(parentClass = XrayHooks.class, name = "isXrayBlocks") ASMMethod isXrayEnabled,
      @MapMethod(parentClass = XrayHooks.class, name = "changeBrightness") ASMMethod changeBrightness,
      @MapMethod(parentClass = XrayHooks.class, name = "getBlockAlphaOverride") ASMMethod getBlockAlphaOverride) {
    LocalVariableNode lvLights = ASMHelper.getLocalVariable(node, "combinedLightsIn", Type.getType(int[].class))
        .orElseThrow(() -> new Error("Could not find local variable combinedLightsIn"));
    LocalVariableNode lvColors = ASMHelper.getLocalVariable(node, "colorMuls", Type.getType(float[].class))
        .orElseThrow(() -> new Error("Could not find local variable colorMuls"));
    LocalVariableNode lvAlpha = ASMHelper.getLocalVariable(node, "alphaIn", Type.getType(float.class))
        .orElseThrow(() -> new Error("Could not find local variable alphaIn"));

    LabelNode end = new LabelNode();

    InsnList list = new InsnList();

    // only run if xray is enabled
    list.add(ASMHelper.call(INVOKESTATIC, isXrayEnabled));
    list.add(new JumpInsnNode(IFEQ, end));

    // set the brightness to full bright if this is an xray block
    // if its a transparent block, we will continue and set the alpha
    // to our desired value
    list.add(new VarInsnNode(ALOAD, lvLights.index));
    list.add(new VarInsnNode(ALOAD, lvColors.index));
    list.add(ASMHelper.call(INVOKESTATIC, changeBrightness));
    list.add(new JumpInsnNode(IFEQ, end));

    list.add(ASMHelper.call(INVOKESTATIC, getBlockAlphaOverride));
    list.add(new VarInsnNode(FSTORE, lvAlpha.index));

    list.add(end);

    node.instructions.insert(list);
  }
}
