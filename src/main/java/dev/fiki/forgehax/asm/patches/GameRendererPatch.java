package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.asm.MapClass;
import dev.fiki.forgehax.api.asm.MapMethod;
import dev.fiki.forgehax.asm.events.render.NearClippingPlaneEvent;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMClass;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.client.renderer.GameRenderer;
import org.objectweb.asm.tree.*;

@MapClass(GameRenderer.class)
public class GameRendererPatch extends Patch {
  @Inject
  @MapMethod("bobHurt")
  public void bobHurt(MethodNode node,
      @MapMethod(parentClass = ForgeHaxHooks.class, name = "shouldStopHurtcamEffect") ASMMethod callback) {
    AbstractInsnNode returnCall = ASMPattern.builder()
        .codeOnly()
        .opcodes(RETURN)
        .find(node)
        .getLast("Could not find return call");

    LabelNode jmp = new LabelNode();

    InsnList list = new InsnList();
    // call hurtcam event
    list.add(ASMHelper.call(INVOKESTATIC, callback));
    // do not call if hurtcam event cancels execution
    list.add(new JumpInsnNode(IFNE, jmp));

    node.instructions.insert(list);
    node.instructions.insertBefore(returnCall, jmp);
  }

  @Inject
  @MapMethod("getProjectionMatrix")
  public void getProjectionMatrix(MethodNode node,
      @MapClass(NearClippingPlaneEvent.class) ASMClass nearClippingPlaneEvent,
      @MapMethod(parentClass = ForgeHaxHooks.class, name = "fireEvent_v") ASMMethod fireEvent) {
    LdcInsnNode nearPlaneNum = ASMPattern.builder().codeOnly()
        .constant(0.05F)
        .find(node)
        .getFirst("Failed to find near plane constant");

    final InsnList params = new InsnList();
    params.add(new LdcInsnNode(nearPlaneNum.cst));
    final InsnList newEvent = ASMHelper.newInstance(nearClippingPlaneEvent.getClassName(), "(F)V", params);

    final InsnList list = new InsnList();
    list.add(newEvent);
    list.add(new InsnNode(DUP));
    list.add(ASMHelper.call(INVOKESTATIC, fireEvent));
    list.add(new FieldInsnNode(GETFIELD, nearClippingPlaneEvent.getClassName(), "value", "F"));

    node.instructions.insert(nearPlaneNum, list);
    node.instructions.remove(nearPlaneNum);
  }
}
