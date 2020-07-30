package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.events.NearClippingPlaneEvent;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMClass;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.eventbus.api.Event;
import org.objectweb.asm.tree.*;

@ClassMapping(GameRenderer.class)
public class GameRendererPatch extends Patch {
  @Inject
  @MethodMapping("hurtCameraEffect")
  public void hurtCameraEffect(MethodNode node,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "shouldStopHurtcamEffect",
          ret = boolean.class) ASMMethod callback) {
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
  @MethodMapping("getProjectionMatrix")
  public void getProjectionMatrix(MethodNode node,
      @ClassMapping(NearClippingPlaneEvent.class) ASMClass nearClippingPlaneEvent,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "fireEvent_v",
          args = Event.class) ASMMethod fireEvent) {
    LdcInsnNode nearPlaneNum = ASMPattern.builder().codeOnly()
        .constant(0.05F)
        .find(node)
        .getFirst("Failed to find near plane constant");

    final InsnList params = new InsnList();
    params.add(new LdcInsnNode(nearPlaneNum.cst));
    final InsnList newEvent = ASMHelper.newInstance(nearClippingPlaneEvent.getName(), "(F)V", params);

    final InsnList list = new InsnList();
    list.add(newEvent);
    list.add(new InsnNode(DUP));
    list.add(ASMHelper.call(INVOKESTATIC, fireEvent));
    list.add(new FieldInsnNode(GETFIELD, nearClippingPlaneEvent.getName(), "value", "F"));

    node.instructions.insert(nearPlaneNum, list);
    node.instructions.remove(nearPlaneNum);
  }
}
