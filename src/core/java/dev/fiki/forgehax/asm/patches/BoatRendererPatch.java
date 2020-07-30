package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.entity.item.BoatEntity;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Created by Babbaj on 8/9/2017.
 */
@ClassMapping(BoatRenderer.class)
public class BoatRendererPatch extends Patch {

  @Inject
  @MethodMapping("render")
  public void render(MethodNode main,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "onRenderBoat",
          args = {BoatEntity.class, float.class},
          ret = float.class) ASMMethod renderBoat) {
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
