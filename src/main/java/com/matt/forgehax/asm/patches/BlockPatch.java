package com.matt.forgehax.asm.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import com.matt.forgehax.asm.utils.transforming.Inject;
import com.matt.forgehax.asm.utils.transforming.InjectPriority;
import com.matt.forgehax.asm.utils.transforming.MethodTransformer;
import com.matt.forgehax.asm.utils.transforming.RegisterMethodTransformer;
import java.util.Objects;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class BlockPatch extends ClassTransformer {

  public BlockPatch() {
    super(Classes.Block);
  }

  @RegisterMethodTransformer
  private class CanRenderInLayer extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return Methods.Block_canRenderInLayer;
    }

    @Inject(description = "Changes in layer code so that we can change it")
    public void inject(MethodNode main) {
      AbstractInsnNode node =
        ASMHelper.findPattern(main.instructions.getFirst(), new int[]{INVOKEVIRTUAL}, "x");

      Objects.requireNonNull(node, "Find pattern failed for node");

      InsnList insnList = new InsnList();

      // starting after INVOKEVIRTUAL on Block.getBlockLayer()

      insnList.add(new VarInsnNode(ASTORE, 3)); // store the result from getBlockLayer()
      insnList.add(new VarInsnNode(ALOAD, 0)); // push this
      insnList.add(new VarInsnNode(ALOAD, 1)); // push block state
      insnList.add(new VarInsnNode(ALOAD, 3)); // push this.getBlockLayer() result
      insnList.add(
        new VarInsnNode(ALOAD, 2)); // push the block layer of the block we are comparing to
      insnList.add(
        ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onRenderBlockInLayer));
      // now our result is on the stack

      main.instructions.insert(node, insnList);
    }
  }

  @RegisterMethodTransformer
  private class AddCollisionBoxToList extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return Methods.Block_addCollisionBoxToList;
    }

    @Inject(
      description =
        "Redirects method to our hook and allows the vanilla code to be canceled from executing",
      priority = InjectPriority.LOWEST
    )
    public void inject(MethodNode main) {
      AbstractInsnNode node = main.instructions.getFirst();
      AbstractInsnNode end =
        ASMHelper.findPattern(main.instructions.getFirst(), new int[]{RETURN}, "x");

      Objects.requireNonNull(node, "Find pattern failed for node");
      Objects.requireNonNull(end, "Find pattern failed for end");

      LabelNode jumpPast = new LabelNode();

      InsnList insnList = new InsnList();
      insnList.add(new VarInsnNode(ALOAD, 0)); // block
      insnList.add(new VarInsnNode(ALOAD, 1)); // state
      insnList.add(new VarInsnNode(ALOAD, 2)); // world
      insnList.add(new VarInsnNode(ALOAD, 3)); // pos
      insnList.add(new VarInsnNode(ALOAD, 4)); // entityBox
      insnList.add(new VarInsnNode(ALOAD, 5)); // collidingBoxes
      insnList.add(new VarInsnNode(ALOAD, 6)); // entityIn
      insnList.add(new VarInsnNode(ILOAD, 7)); // bool
      insnList.add(
        ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onAddCollisionBoxToList));
      insnList.add(new JumpInsnNode(IFNE, jumpPast));

      main.instructions.insertBefore(end, jumpPast);
      main.instructions.insertBefore(node, insnList);
    }
  }
}
