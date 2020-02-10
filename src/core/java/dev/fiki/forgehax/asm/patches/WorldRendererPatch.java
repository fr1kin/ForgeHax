package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesMc;

public class WorldRendererPatch {
  

//  @RegisterTransformer("Add hook at the top of the method")
//  public static class DrawBoundingBox extends MethodTransformer {
//
//    @Override
//    public ASMMethod getMethod() {
//      return TypesMc.Methods.WorldRenderer_drawBoundingBox;
//    }
//
//    @Override
//    public void transform(MethodNode main) {
//      AbstractInsnNode start = main.instructions.getFirst();
//      AbstractInsnNode end = ASMHelper.findPattern(start, RETURN);
//
//      final int eventIndex =
//        ASMHelper.addNewLocalVariable(
//          main, "forgehax_event", Type.getDescriptor(DrawBlockBoundingBoxEvent.Pre.class));
//
//      InsnList pushArgs = new InsnList();
//      pushArgs.add(new VarInsnNode(FLOAD, 12));
//      pushArgs.add(new VarInsnNode(FLOAD, 13));
//      pushArgs.add(new VarInsnNode(FLOAD, 14));
//      pushArgs.add(new VarInsnNode(FLOAD, 15));
//
//      InsnList newEvent =
//        ASMHelper.newInstance(
//          Type.getInternalName(DrawBlockBoundingBoxEvent.Pre.class), "(FFFF)V", pushArgs);
//
//      final InsnList pre = new InsnList();
//      pre.add(newEvent);
//      pre.add(new VarInsnNode(ASTORE, eventIndex));
//      pre.add(new VarInsnNode(ALOAD, eventIndex));
//      pre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_fireEvent_v));
//      pre.add(setColor(eventIndex, "red", 12));
//      pre.add(setColor(eventIndex, "green", 13));
//      pre.add(setColor(eventIndex, "blue", 14));
//      pre.add(setColor(eventIndex, "alpha", 15));
//
//      final InsnList post = new InsnList();
//      post.add(
//        ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onDrawBoundingBox_Post));
//
//      main.instructions.insert(start, pre);
//      main.instructions.insertBefore(end, post);
//    }
//
//    private InsnList setColor(int eventIndex, String field, int colorIndex) {
//      InsnList list = new InsnList();
//      list.add(new VarInsnNode(ALOAD, eventIndex));
//      list.add(
//        new FieldInsnNode(
//          GETFIELD, Type.getInternalName(DrawBlockBoundingBoxEvent.class), field, "F"));
//      list.add(new VarInsnNode(FSTORE, colorIndex));
//
//      return list;
//    }
//  }
}
