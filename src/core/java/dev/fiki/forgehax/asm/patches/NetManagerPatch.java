package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.InsnPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import org.objectweb.asm.tree.*;

@ClassMapping(NetworkManager.class)
public class NetManagerPatch extends Patch {

  @Inject
  @MethodMapping("dispatchPacket")
  public void dispatchPacket(MethodNode main,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "onPacketOutbound",
          args = {NetworkManager.class, IPacket.class},
          ret = boolean.class
      ) ASMMethod hook) {
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
    pre.add(ASMHelper.call(INVOKESTATIC, hook));
    pre.add(new JumpInsnNode(IFNE, jmp));

    // add just below the first node (which should be a label node)
    main.instructions.insert(top, pre);
    // add just before the return node
    main.instructions.insertBefore(ret, jmp);
  }

  @Inject
  @MethodMapping("channelRead0")
  public void channelRead0(MethodNode main,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "onPacketInbound",
          args = {NetworkManager.class, IPacket.class},
          ret = boolean.class
      ) ASMMethod hook) {
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
    pre.add(ASMHelper.call(INVOKESTATIC, hook));
    pre.add(new JumpInsnNode(IFNE, jmp));

    // add just below the first node (which should be a label node)
    main.instructions.insertBefore(first, pre);
    // add just before the return node
    main.instructions.insertBefore(ret, jmp);
  }
}
