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

public class NetManager$4Patch extends ClassTransformer {
  public NetManager$4Patch() {
    super(Classes.NetworkManager$4);
  }

  @RegisterMethodTransformer
  private class Run extends MethodTransformer {
    @Override
    public ASMMethod getMethod() {
      return Methods.NetworkManager$4_run;
    }

    @Inject(description = "Add a pre and post hook that allows the method to be disabled")
    public void inject(MethodNode main) {
      AbstractInsnNode preNode =
          ASMHelper.findPattern(
              main.instructions.getFirst(),
              new int[] {ALOAD, GETFIELD, ALOAD, GETFIELD, IF_ACMPEQ},
              "xxxxx");

      AbstractInsnNode postNode =
          ASMHelper.findPattern(main.instructions.getFirst(), new int[] {RETURN}, "x");

      Objects.requireNonNull(preNode, "Find pattern failed for preNode");
      Objects.requireNonNull(postNode, "Find pattern failed for postNode");

      LabelNode endJump = new LabelNode();

      InsnList insnPre = new InsnList();
      insnPre.add(new VarInsnNode(ALOAD, 0));
      insnPre.add(ASMHelper.call(GETFIELD, Fields.NetworkManager$4_val$inPacket));
      insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSendingPacket));
      insnPre.add(new JumpInsnNode(IFNE, endJump));

      InsnList insnPost = new InsnList();
      insnPost.add(new VarInsnNode(ALOAD, 0));
      insnPost.add(ASMHelper.call(GETFIELD, Fields.NetworkManager$4_val$inPacket));
      insnPost.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSentPacket));
      insnPost.add(endJump);

      main.instructions.insertBefore(preNode, insnPre);
      main.instructions.insertBefore(postNode, insnPost);
    }
  }
}
