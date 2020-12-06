package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.asm.MapClass;
import dev.fiki.forgehax.api.asm.MapMethod;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.client.renderer.entity.BoatRenderer;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Created by Babbaj on 8/9/2017.
 */
@MapClass(BoatRenderer.class)
public class BoatRendererPatch extends Patch {

  @Inject
  @MapMethod("render")
  public void render(MethodNode main,
      @MapMethod(parentClass = ForgeHaxHooks.class, name = "onRenderBoat") ASMMethod renderBoat) {
    InsnList insnList = new InsnList();

    insnList.add(new VarInsnNode(ALOAD, 1)); // load the boat entity
    insnList.add(new VarInsnNode(FLOAD, 2)); // load the boat yaw
    insnList.add(ASMHelper.call(INVOKESTATIC, renderBoat));
    // fire the event and get the value(player rotationYaw) returned by the method in
    // ForgeHaxHooks
    insnList.add(new VarInsnNode(FSTORE, 2)); // store it in entityYaw

    main.instructions.insert(insnList); // insert code at the top of the method
  }
}
