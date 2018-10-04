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

public class NetManagerPatch extends ClassTransformer {
  public NetManagerPatch() {
    super(Classes.NetworkManager);
  }

  @RegisterMethodTransformer
  private class DispatchPacket extends MethodTransformer {
    @Override
    public ASMMethod getMethod() {
      return Methods.NetworkManager_dispatchPacket;
    }

    @Inject(description = "Add pre and post hooks that allow method to be disabled")
    public void inject(MethodNode main) {
      AbstractInsnNode preNode =
          ASMHelper.findPattern(
              main.instructions.getFirst(),
              new int[] {
                ALOAD,
                ALOAD,
                IF_ACMPEQ,
                ALOAD,
                INSTANCEOF,
                IFNE,
                0x00,
                0x00,
                ALOAD,
                ALOAD,
                INVOKEVIRTUAL
              },
              "xxxxxx??xxx");
      AbstractInsnNode postNode =
          ASMHelper.findPattern(
              main.instructions.getFirst(),
              new int[] {
                POP, 0x00, 0x00, GOTO, 0x00, 0x00, 0x00, ALOAD, GETFIELD, INVOKEINTERFACE, NEW, DUP
              },
              "x??x???xxxxx");

      Objects.requireNonNull(preNode, "Find pattern failed for preNode");
      Objects.requireNonNull(postNode, "Find pattern failed for postNode");

      LabelNode endJump = new LabelNode();

      InsnList insnPre = new InsnList();
      insnPre.add(new VarInsnNode(ALOAD, 1));
      insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSendingPacket));
      insnPre.add(new JumpInsnNode(IFNE, endJump));

      InsnList insnPost = new InsnList();
      insnPost.add(new VarInsnNode(ALOAD, 1));
      insnPost.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onSentPacket));
      insnPost.add(endJump);

      main.instructions.insertBefore(preNode, insnPre);
      main.instructions.insert(postNode, insnPost);
    }
  }

  @RegisterMethodTransformer
  private class ChannelRead0 extends MethodTransformer {
    @Override
    public ASMMethod getMethod() {
      return Methods.NetworkManager_channelRead0;
    }

    @Inject(description = "Add pre and post hook that allows the method to be disabled")
    public void inject(MethodNode main) {
      AbstractInsnNode preNode =
          ASMHelper.findPattern(
              main.instructions.getFirst(),
              new int[] {ALOAD, ALOAD, GETFIELD, INVOKEINTERFACE},
              "xxxx");
      AbstractInsnNode postNode =
          ASMHelper.findPattern(
              main.instructions.getFirst(),
              new int[] {
                INVOKEINTERFACE, 0x00, 0x00, GOTO,
              },
              "x??x");

      Objects.requireNonNull(preNode, "Find pattern failed for preNode");
      Objects.requireNonNull(postNode, "Find pattern failed for postNode");

      LabelNode endJump = new LabelNode();

      InsnList insnPre = new InsnList();
      insnPre.add(new VarInsnNode(ALOAD, 2));
      insnPre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPreReceived));
      insnPre.add(new JumpInsnNode(IFNE, endJump));

      InsnList insnPost = new InsnList();
      insnPost.add(new VarInsnNode(ALOAD, 2));
      insnPost.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPostReceived));
      insnPost.add(endJump);

      main.instructions.insertBefore(preNode, insnPre);
      main.instructions.insert(postNode, insnPost);
    }
  }
}
