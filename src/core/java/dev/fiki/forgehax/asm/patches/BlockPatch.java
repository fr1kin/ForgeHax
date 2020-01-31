package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.common.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.ClassTransformer;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import org.objectweb.asm.tree.*;

public class BlockPatch extends ClassTransformer {
  
  public BlockPatch() {
    super(TypesMc.Classes.Block);
  }

  // REMOVED in 1.15
//  @RegisterMethodTransformer
//  private class CanRenderInLayer extends MethodTransformer {
//
//    @Override
//    public ASMMethod getMethod() {
//      return TypesMc.Methods.Block_canRenderInLayer;
//    }
//
//    @Inject(description = "Changes in layer code so that we can change it")
//    public void inject(MethodNode main) {
//      AbstractInsnNode node =
//        ASMHelper.findPattern(main.instructions.getFirst(), new int[]{INVOKEVIRTUAL}, "x");
//
//      Objects.requireNonNull(node, "Find pattern failed for node");
//
//      InsnList insnList = new InsnList();
//
//      // starting after INVOKEVIRTUAL on Block.getBlockLayer()
//
//      insnList.add(new VarInsnNode(ASTORE, 3)); // store the result from getBlockLayer()
//      insnList.add(new VarInsnNode(ALOAD, 0)); // push this
//      insnList.add(new VarInsnNode(ALOAD, 1)); // push block state
//      insnList.add(new VarInsnNode(ALOAD, 3)); // push this.getBlockLayer() result
//      insnList.add(
//        new VarInsnNode(ALOAD, 2)); // push the block layer of the block we are comparing to
//      insnList.add(
//        ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onRenderBlockInLayer));
//      // now our result is on the stack
//
//      main.instructions.insert(node, insnList);
//    }
//  }

  // REMOVED in 1.15
//  @RegisterMethodTransformer
//  private class AddCollisionBoxToList extends MethodTransformer {
//
//    @Override
//    public ASMMethod getMethod() {
//      return TypesMc.Methods.Block_addCollisionBoxToList;
//    }
//
//    @Inject(
//      description =
//        "Redirects method to our hook and allows the vanilla code to be canceled from executing")
//    public void inject(MethodNode main) {
//      AbstractInsnNode node = main.instructions.getFirst();
//      AbstractInsnNode end =
//        ASMHelper.findPattern(main.instructions.getFirst(), new int[]{RETURN}, "x");
//
//      Objects.requireNonNull(node, "Find pattern failed for node");
//      Objects.requireNonNull(end, "Find pattern failed for end");
//
//      LabelNode jumpPast = new LabelNode();
//
//      InsnList insnList = new InsnList();
//      insnList.add(new VarInsnNode(ALOAD, 0)); // block
//      insnList.add(new VarInsnNode(ALOAD, 1)); // state
//      insnList.add(new VarInsnNode(ALOAD, 2)); // world
//      insnList.add(new VarInsnNode(ALOAD, 3)); // pos
//      insnList.add(new VarInsnNode(ALOAD, 4)); // entityBox
//      insnList.add(new VarInsnNode(ALOAD, 5)); // collidingBoxes
//      insnList.add(new VarInsnNode(ALOAD, 6)); // entityIn
//      insnList.add(new VarInsnNode(ILOAD, 7)); // bool
//      insnList.add(
//        ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onAddCollisionBoxToList));
//      insnList.add(new JumpInsnNode(IFNE, jumpPast));
//
//      main.instructions.insertBefore(end, jumpPast);
//      main.instructions.insertBefore(node, insnList);
//    }
//  }


  @RegisterMethodTransformer
  public static class GetCollisionShape extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return Methods.Block_getCollisionShape;
    }

    @Inject
    public void transform(MethodNode method) {
      // TODO: 1.15 maybe
//      AbstractInsnNode node = method.instructions.getFirst();
//
//      LabelNode jump = new LabelNode();
//      final int eventIdx = ASMHelper.addNewLocalVariable(method, "forgehax_event",
//          TypesHook.Classes.GetCollisionShapeEvent.getClassDescriptor());
//
//      InsnList list = new InsnList();
//      InsnList eventArgs = new InsnList();
//      eventArgs.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this (Block)
//      eventArgs.add(new VarInsnNode(Opcodes.ALOAD, 1)); // state
//      eventArgs.add(new VarInsnNode(Opcodes.ALOAD, 2)); // worldIn (IBlockReader)
//      eventArgs.add(new VarInsnNode(Opcodes.ALOAD, 3)); // pos
//      list.add(ASMHelper.newInstance(TypesHook.Classes.GetCollisionShapeEvent.getClassName(),
//          new ASMClass[]{Classes.Block, Classes.BlockState, Classes.IBlockReader, Classes.BlockPos},
//          eventArgs));
//      list.add(new VarInsnNode(Opcodes.ASTORE, eventIdx));
//      list.add(new VarInsnNode(Opcodes.ALOAD, eventIdx));
//      list.add(ASMHelper.call(Opcodes.INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_fireEvent_b));
//      list.add(new JumpInsnNode(Opcodes.IFNE, jump));
//      list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(GetCollisionShapeEvent.class), "getReturnShape", "()" + Classes.VoxelShape.getDescriptor()));
//      list.add(new InsnNode(Opcodes.ARETURN));
//      list.add(jump);
    }
  }
}
