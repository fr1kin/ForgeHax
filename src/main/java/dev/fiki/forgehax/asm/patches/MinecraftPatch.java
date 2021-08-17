package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.asm.MapClass;
import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.asm.MapMethod;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMField;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.client.Minecraft;
import org.objectweb.asm.tree.*;

@MapClass(Minecraft.class)
public class MinecraftPatch extends Patch {

  @Inject
  @MapMethod("tick")
  public void tick(MethodNode method,
      @MapMethod(parentClass = ForgeHaxHooks.class, name = "onLeftClickCounterSet") ASMMethod hook,
      @MapField("missTime") ASMField missTime) {
    // this.leftClickCounter = 10000;
    AbstractInsnNode node = ASMPattern.builder()
        .opcodes(SIPUSH)
        .custom(n -> {
          if (n instanceof FieldInsnNode && n.getOpcode() == PUTFIELD) {
            FieldInsnNode fld = (FieldInsnNode) n;
            return missTime.anyNameEquals(fld.name);
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
  @MapMethod("continueAttack")
  public void continueAttack(MethodNode method,
      @MapMethod(parentClass = ForgeHaxHooks.class, name = "onSendClickBlockToController") ASMMethod hook) {
    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(new VarInsnNode(ILOAD, 1));
    list.add(ASMHelper.call(INVOKESTATIC, hook));
    list.add(new VarInsnNode(ISTORE, 1));

    method.instructions.insert(list);
  }
}
