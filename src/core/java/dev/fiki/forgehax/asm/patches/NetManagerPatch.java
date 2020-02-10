package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.InsnPattern;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import dev.fiki.forgehax.asm.TypesMc;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class NetManagerPatch {

  @RegisterTransformer("ForgeHaxHooks::onPacketOutbound")
  public static class DispatchPacket extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.NetworkManager_dispatchPacket;
    }

    @Override
    public void transform(MethodNode main) {
      // get node at the very top
      AbstractInsnNode top = main.instructions.getFirst();

      // get the return node
      AbstractInsnNode ret = ASMPattern.builder()
          .codeOnly()
          .opcode(RETURN)
          .find(main)
          .getFirst("could not find return node");

      LabelNode jmp = new LabelNode();

      // call event before all other code
      InsnList pre = new InsnList();
      pre.add(new VarInsnNode(ALOAD, 0)); // network manager
      pre.add(new VarInsnNode(ALOAD, 1)); // packet
      pre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPacketOutbound));
      pre.add(new JumpInsnNode(IFNE, jmp));

      // add just below the first node (which should be a label node)
      main.instructions.insert(top, pre);
      // add just before the return node
      main.instructions.insertBefore(ret, jmp);
    }
  }

  @RegisterTransformer("ForgeHaxHooks::onPacketInbound")
  public static class ChannelRead0 extends MethodTransformer {

    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.NetworkManager_channelRead0;
    }

    @Override
    public void transform(MethodNode main) {
      // try {
      // >FIRST<
      // processPacket(...
      InsnPattern node = ASMPattern.builder()
          .codeOnly()
          .opcodes(ALOAD, ALOAD, GETFIELD, INVOKESTATIC)
          .find(main);

      AbstractInsnNode first = node.getFirst("could not find node above ::processPacket call");

      // get the return node
      // dont add jump after ::processPacket because there is a field that increments
      // if ThreadQuickExitException is not thrown (which is nearly always)
      AbstractInsnNode ret = ASMPattern.builder()
          .codeOnly()
          .opcodes(RETURN)
          .find(main)
          .getFirst("could not find return node");

      LabelNode jmp = new LabelNode();

      // call event before all other code
      InsnList pre = new InsnList();
      pre.add(new VarInsnNode(ALOAD, 0)); // network manager
      pre.add(new VarInsnNode(ALOAD, 2)); // packet
      pre.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onPacketInbound));
      pre.add(new JumpInsnNode(IFNE, jmp));

      // add just below the first node (which should be a label node)
      main.instructions.insertBefore(first, pre);
      // add just before the return node
      main.instructions.insertBefore(ret, jmp);
    }
  }
}
