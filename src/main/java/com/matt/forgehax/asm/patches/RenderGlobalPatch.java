package com.matt.forgehax.asm.patches;

import static org.objectweb.asm.Opcodes.*;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import java.util.Objects;
import org.objectweb.asm.tree.*;

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
              new int[] {PUTFIELD, 0x00, 0x00, 0x00, RETURN},
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
              new int[] {
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
              new int[] {ALOAD, GETFIELD, GETFIELD, INVOKEVIRTUAL, 0x00, 0x00, ILOAD, IRETURN},
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
              new int[] {ALOAD, GETFIELD, GETFIELD, GETFIELD, ALOAD},
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
              new int[] {
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
}
