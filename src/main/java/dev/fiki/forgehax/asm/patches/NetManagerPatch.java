package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.asm.MapClass;
import dev.fiki.forgehax.api.asm.MapMethod;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.InsnPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.network.NetworkManager;
import org.objectweb.asm.tree.*;

@MapClass(NetworkManager.class)
public class NetManagerPatch extends Patch {

  @Inject
  @MapMethod("sendPacket")
  public void sendPacket(MethodNode main,
      @MapMethod(parentClass = ForgeHaxHooks.class, name = "onPacketOutbound") ASMMethod hook) {
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
  @MapMethod("channelRead0")
  public void channelRead0(MethodNode main,
      @MapMethod(parentClass = ForgeHaxHooks.class, name = "onPacketInbound") ASMMethod hook) {
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
