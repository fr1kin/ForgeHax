package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMField;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.client.Minecraft;
import org.objectweb.asm.tree.*;

@ClassMapping(Minecraft.class)
public class MinecraftPatch extends Patch {

  @Inject
  @MethodMapping("runTick")
  public void runTick(MethodNode method,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "onLeftClickCounterSet",
          args = {int.class, Minecraft.class},
          ret = int.class
      ) ASMMethod hook,
      @FieldMapping("leftClickCounter") ASMField leftClickCounter) {
    // this.leftClickCounter = 10000;
    AbstractInsnNode node = ASMPattern.builder()
        .opcodes(SIPUSH)
        .custom(n -> {
          if (n instanceof FieldInsnNode && n.getOpcode() == PUTFIELD) {
            FieldInsnNode fld = (FieldInsnNode) n;
            return leftClickCounter.isNameEqual(fld.name);
          }
          return false;
        })
        .find(method)
        .getFirst();

    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(ASMHelper.call(INVOKESTATIC, hook));

    method.instructions.insert(node, list);
  }

  @Inject
  @MethodMapping("sendClickBlockToController")
  public void sendClickBlockToController(MethodNode method,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "onSendClickBlockToController",
          args = {Minecraft.class, boolean.class},
          ret = boolean.class
      ) ASMMethod hook) {
    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(new VarInsnNode(ILOAD, 1));
    list.add(ASMHelper.call(INVOKESTATIC, hook));
    list.add(new VarInsnNode(ISTORE, 1));

    method.instructions.insert(list);
  }
}
