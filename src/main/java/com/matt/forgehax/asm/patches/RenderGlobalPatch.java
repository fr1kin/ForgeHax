package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.events.DrawBlockBoundingBoxEvent;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import java.util.Objects;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import scala.tools.asm.Type;

public class RenderGlobalPatch extends ClassTransformer {

  public RenderGlobalPatch() {
    super(Classes.RenderGlobal);
  }

  @RegisterMethodTransformer
  private class LoadRenderers extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return Methods.RenderGlobal_loadRenderers;
    }

    @Inject(description = "At hook callback at end of method")
    public void inject(MethodNode main) {
      AbstractInsnNode node =
        ASMHelper.findPattern(
          main.instructions.getFirst(),
          new int[]{PUTFIELD, 0x00, 0x00, 0x00, RETURN},
          "x???x");

      Objects.requireNonNull(node, "Find pattern failed for node");

      InsnList insnList = new InsnList();
      insnList.add(new VarInsnNode(ALOAD, 0)); // push this
      insnList.add(ASMHelper.call(GETFIELD, Fields.RenderGlobal_viewFrustum));
      insnList.add(new VarInsnNode(ALOAD, 0)); // push this
      insnList.add(ASMHelper.call(GETFIELD, Fields.RenderGlobal_renderDispatcher));
      insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onLoadRenderers));

      main.instructions.insert(node, insnList);
    }
  }

  @RegisterMethodTransformer
  private class RenderBlockLayer extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return Methods.RenderGlobal_renderBlockLayer;
    }

    @Inject(description = "Add hooks at the top and bottom of the method")
    public void inject(MethodNode main) {
      AbstractInsnNode preNode =
        ASMHelper.findPattern(
          main.instructions.getFirst(),
          new int[]{
            INVOKESTATIC,
            0x00,
            0x00,
            ALOAD,
            GETSTATIC,
            IF_ACMPNE,
            0x00,
            0x00,
            ALOAD,
            GETFIELD,
            GETFIELD
          },
          "x??xxx??xxx");
      AbstractInsnNode postNode =
        ASMHelper.findPattern(
          main.instructions.getFirst(),
          new int[]{ALOAD, GETFIELD, GETFIELD, INVOKEVIRTUAL, 0x00, 0x00, ILOAD, IRETURN},
          "xxxx??xx");

      Objects.requireNonNull(preNode, "Find pattern failed for preNode");
      Objects.requireNonNull(postNode, "Find pattern failed for postNode");

      LabelNode endJump = new LabelNode();

      InsnList insnPre = new InsnList();
      insnPre.add(new InsnNode(ICONST_0));
      insnPre.add(new VarInsnNode(ISTORE, 6));
      insnPre.add(new VarInsnNode(ALOAD, 1));
      insnPre.add(new VarInsnNode(DLOAD, 2));
      insnPre.add(
        ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPreRenderBlockLayer));
      insnPre.add(new JumpInsnNode(IFNE, endJump));

      InsnList insnPost = new InsnList();
      insnPost.add(new VarInsnNode(ALOAD, 1));
      insnPost.add(new VarInsnNode(DLOAD, 2));
      insnPost.add(
        ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPostRenderBlockLayer));
      insnPost.add(endJump);

      main.instructions.insertBefore(preNode, insnPre);
      main.instructions.insertBefore(postNode, insnPost);
    }
  }

  @RegisterMethodTransformer
  private class SetupTerrain extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return Methods.RenderGlobal_setupTerrain;
    }

    @Inject(description = "Add hook at the top of the method")
    public void inject(MethodNode main) {
      AbstractInsnNode node =
        ASMHelper.findPattern(
          main.instructions.getFirst(),
          new int[]{ALOAD, GETFIELD, GETFIELD, GETFIELD, ALOAD},
          "xxxxx");

      Objects.requireNonNull(node, "Find pattern failed for node");

      InsnList insnPre = new InsnList();
      insnPre.add(new VarInsnNode(ALOAD, 1));
      insnPre.add(new VarInsnNode(ILOAD, 6));
      insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSetupTerrain));
      insnPre.add(new VarInsnNode(ISTORE, 6));

      main.instructions.insertBefore(node, insnPre);
    }

    @Inject(description = "Add or logic to this.mc.renderChunksMany flag")
    public void injectAtFlag(MethodNode main) {
      // inject at this.mc.renderChunksMany
      AbstractInsnNode node =
        ASMHelper.findPattern(
          main.instructions.getFirst(),
          new int[]{
            ISTORE,
            0x00,
            0x00,
            ALOAD,
            IFNULL,
            0x00,
            0x00,
            ICONST_0,
            ISTORE,
            0x00,
            0x00,
            NEW,
            DUP,
            ALOAD,
            ALOAD,
            ACONST_NULL,
            CHECKCAST,
            ICONST_0,
            ACONST_NULL,
            INVOKESPECIAL,
            ASTORE
          },
          "x??xx??xx??xxxxxxxxxx");

      Objects.requireNonNull(node, "Find pattern failed for node");

      LabelNode storeLabel = new LabelNode();
      LabelNode falseLabel = new LabelNode();

      InsnList insnList = new InsnList();
      insnList.add(new JumpInsnNode(IFEQ, falseLabel));
      insnList.add(
        ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_shouldDisableCaveCulling));
      insnList.add(new JumpInsnNode(IFNE, falseLabel));
      insnList.add(new InsnNode(ICONST_1));
      insnList.add(new JumpInsnNode(GOTO, storeLabel));
      insnList.add(falseLabel);
      insnList.add(new InsnNode(ICONST_0));
      insnList.add(storeLabel);
      // iload should be below here

      main.instructions.insertBefore(node, insnList);
    }
  }

  @RegisterMethodTransformer
  private class DrawBoundingBox extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return Methods.RenderGlobal_drawBoundingBox;
    }

    @Inject(description = "Add hook at the top of the method")
    public void inject(MethodNode main) {
      AbstractInsnNode start = main.instructions.getFirst();
      AbstractInsnNode end = ASMHelper.findPattern(start, RETURN);

      final int eventIndex =
        ASMHelper.addNewLocalVariable(
          main, "forgehax_event", Type.getDescriptor(DrawBlockBoundingBoxEvent.Pre.class));

      InsnList pushArgs = new InsnList();
      pushArgs.add(new VarInsnNode(FLOAD, 12));
      pushArgs.add(new VarInsnNode(FLOAD, 13));
      pushArgs.add(new VarInsnNode(FLOAD, 14));
      pushArgs.add(new VarInsnNode(FLOAD, 15));

      InsnList newEvent =
        ASMHelper.newInstance(
          Type.getInternalName(DrawBlockBoundingBoxEvent.Pre.class), "(FFFF)V", pushArgs);

      final InsnList pre = new InsnList();
      pre.add(newEvent);
      pre.add(new VarInsnNode(ASTORE, eventIndex));
      pre.add(new VarInsnNode(ALOAD, eventIndex));
      pre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_fireEvent_v));
      pre.add(setColor(eventIndex, "red", 12));
      pre.add(setColor(eventIndex, "green", 13));
      pre.add(setColor(eventIndex, "blue", 14));
      pre.add(setColor(eventIndex, "alpha", 15));

      final InsnList post = new InsnList();
      post.add(
        ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onDrawBoundingBox_Post));

      main.instructions.insert(start, pre);
      main.instructions.insertBefore(end, post);
    }

    private InsnList setColor(int eventIndex, String field, int colorIndex) {
      InsnList list = new InsnList();
      list.add(new VarInsnNode(ALOAD, eventIndex));
      list.add(
        new FieldInsnNode(
          GETFIELD, Type.getInternalName(DrawBlockBoundingBoxEvent.class), field, "F"));
      list.add(new VarInsnNode(FSTORE, colorIndex));

      return list;
    }
  }
}
