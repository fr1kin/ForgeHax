package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.hooks.PushHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.objectweb.asm.tree.*;

@ClassMapping(ClientPlayerEntity.class)
public class ClientEntityPlayerPatch extends Patch {

  @Inject
  @MethodMapping("livingTick")
  public void livingTick(MethodNode main,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "shouldSlowdownPlayer",
          args = {ClientPlayerEntity.class},
          ret = boolean.class
      ) ASMMethod hook) {
    AbstractInsnNode skipNode = ASMPattern.builder()
        .codeOnly()
        .opcodes(ALOAD, INVOKEVIRTUAL, IFEQ, ALOAD, INVOKEVIRTUAL, IFNE)
        .find(main)
        .getLast("could not find IFNE node");

    LabelNode skip = ((JumpInsnNode) skipNode).label;

    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(ASMHelper.call(INVOKESTATIC, hook));
    list.add(new JumpInsnNode(IFEQ, skip));

    main.instructions.insert(skipNode, list);
  }

  @Inject
  @MethodMapping("tick")
  public void tick(MethodNode main,
      @MethodMapping(
          parentClass = ClientPlayerEntity.class,
          value = "onUpdateWalkingPlayer"
      ) ASMMethod onUpdateWalkingPlayer,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "onUpdateWalkingPlayerPre",
          args = {ClientPlayerEntity.class},
          ret = boolean.class
      ) ASMMethod updateWalkingPlayerPre,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "onUpdateWalkingPlayerPost",
          args = {ClientPlayerEntity.class},
          ret = void.class
      ) ASMMethod updateWalkingPlayerPost) {
    // <pre>
    // this.onUpdateWalkingPlayer();
    // <post>
    // skip label
    AbstractInsnNode walkingUpdateCall = ASMPattern.builder()
        .custom(n -> {
          if (n instanceof MethodInsnNode) {
            return onUpdateWalkingPlayer.isNameEqual(((MethodInsnNode) n).name);
          }
          return false;
        })
        .find(main)
        .getFirst("could not find node to onUpdateWalkingPlayer");

    LabelNode jmp = new LabelNode();

    InsnList pre = new InsnList();
    pre.add(new VarInsnNode(ALOAD, 0)); // this*
    pre.add(ASMHelper.call(INVOKESTATIC, updateWalkingPlayerPre));
    pre.add(new JumpInsnNode(IFNE, jmp));

    InsnList post = new InsnList();
    post.add(new VarInsnNode(ALOAD, 0)); // this*
    post.add(ASMHelper.call(INVOKESTATIC, updateWalkingPlayerPost));
    post.add(jmp);

    // insert above ALOAD
    main.instructions.insertBefore(walkingUpdateCall.getPrevious(), pre);
    // insert below call
    main.instructions.insert(walkingUpdateCall, post);
  }

  @Inject
  @MethodMapping("isRowingBoat")
  public void isRowingBoat(MethodNode main,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "shouldNotRowBoat",
          args = {ClientPlayerEntity.class},
          ret = boolean.class
      ) ASMMethod hook) {
    AbstractInsnNode ret = ASMPattern.builder()
        .codeOnly()
        .opcode(IRETURN)
        .find(main)
        .getFirst("could not find return node");

    LabelNode end = new LabelNode();
    LabelNode jump = new LabelNode();

    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(ASMHelper.call(INVOKESTATIC, hook));
    list.add(new JumpInsnNode(IFEQ, jump));
    list.add(new InsnNode(ICONST_0));
    list.add(new JumpInsnNode(GOTO, end));
    list.add(jump);

    main.instructions.insert(list);
    main.instructions.insertBefore(ret, end);
  }

  @Inject
  @MethodMapping("shouldBlockPushPlayer")
  public void shouldBlockPushPlayer(MethodNode node,
      @MethodMapping(
          parentClass = PushHooks.class,
          value = "onPushedByBlock",
          args = {ClientPlayerEntity.class},
          ret = boolean.class
      ) ASMMethod onPushedByBlock) {
    InsnNode ret = ASMHelper.findReturn(IRETURN, node);

    LabelNode disabled = new LabelNode();
    LabelNode notDisabled = new LabelNode();

    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(ASMHelper.call(INVOKESTATIC, onPushedByBlock));
    list.add(new JumpInsnNode(IFEQ, notDisabled));
    list.add(new InsnNode(ICONST_0));
    list.add(new JumpInsnNode(GOTO, disabled));
    list.add(notDisabled);

    node.instructions.insert(list);
    node.instructions.insertBefore(ret, disabled);
  }
}
