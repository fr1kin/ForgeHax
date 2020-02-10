package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.common.asmtype.ASMMethod;
import org.objectweb.asm.tree.*;

public class GameRendererPatch {

  @RegisterTransformer
  private static class Hurtcam extends MethodTransformer {
    @Override
    public ASMMethod getMethod() {
      return Methods.GameRenderer_renderWorld;
    }

    @Override
    public void transform(MethodNode node) {
      AbstractInsnNode hurtcamCall = ASMPattern.builder()
          .codeOnly()
          // this::getProjectionMatrix
          // Matrix4f::multiply
          .opcodes(INVOKEVIRTUAL, INVOKEVIRTUAL)
          // this::hurtCameraEffect
          .opcodes(ALOAD, ALOAD, FLOAD, INVOKESPECIAL)
          .find(node)
          .getLast("Could not find hurtcam call");

      AbstractInsnNode preCall = hurtcamCall
          // FLOAD 1
          .getPrevious()
          // ALOAD 7
          .getPrevious()
          // ALOAD 0
          .getPrevious();

      LabelNode jmp = new LabelNode();

      InsnList list = new InsnList();
      // call hurtcam event
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onHurtcamEffect));
      // do not call if hurtcam event cancels execution
      list.add(new JumpInsnNode(IFNE, jmp));

      node.instructions.insertBefore(preCall, list);
      node.instructions.insert(hurtcamCall, jmp);
    }

  }

  @RegisterTransformer
  private static class ProjectionViewSetup extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return Methods.GameRenderer_renderWorld;
    }

    @Override
    public void transform(MethodNode node) {
      AbstractInsnNode beforeViewVectorCall = ASMPattern.builder()
          .codeOnly()
          .opcodes(ALOAD, GETFIELD, GETFIELD, ALOAD, FLOAD, LLOAD, ILOAD, ALOAD, ALOAD, ALOAD, GETFIELD, ALOAD, INVOKEVIRTUAL)
          .find(node)
          .getFirst("Could not find getViewVector call");

      InsnList list = new InsnList();
      // call hurtcam event
      list.add(new VarInsnNode(ALOAD, 4));
      list.add(new VarInsnNode(ALOAD, 9));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSetupProjectionViewMatrix));
      // do not call if hurtcam event cancels execution

      node.instructions.insertBefore(beforeViewVectorCall, list);
    }
  }
}
