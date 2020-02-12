package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.common.asmtype.ASMMethod;
import dev.fiki.forgehax.common.events.NearClippingPlaneEvent;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class GameRendererPatch {

  @RegisterTransformer("ForgeHaxHooks::onHurtcamEffect")
  public static class Hurtcam extends MethodTransformer {
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

  @RegisterTransformer("ForgeHaxHooks::onSetupProjectionViewMatrix")
  public static class ProjectionViewSetup extends MethodTransformer {

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

  @RegisterTransformer("ForgeHaxHooks::nearClippingPlane")
  public static class GetProjectionMatrix extends MethodTransformer {
    @Override
    public ASMMethod getMethod() {
      return Methods.GameRenderer_getProjectionMatrix;
    }

    @Override
    public void transform(MethodNode node) {
      LdcInsnNode nearPlaneNum = ASMPattern.builder().codeOnly()
          .constant(0.05F)
          .find(node)
          .getFirst("Failed to find near plane constant");

      final InsnList eventAargs = new InsnList(); eventAargs.add(new LdcInsnNode(nearPlaneNum.cst));
      final InsnList newEvent = ASMHelper.newInstance(Type.getInternalName(NearClippingPlaneEvent.class), "(F)V", eventAargs);

      final InsnList list = new InsnList();
      list.add(newEvent);
      list.add(new InsnNode(DUP));
      list.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_fireEvent_v));
      list.add(new FieldInsnNode(GETFIELD, Type.getInternalName(NearClippingPlaneEvent.class), "value", "F"));

      node.instructions.insert(nearPlaneNum, list);
      node.instructions.remove(nearPlaneNum);
    }
  }
}
