package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.asm.TypesHook;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.transforming.MethodTransformer;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Created by Babbaj on 8/9/2017.
 */
public class BoatRendererPatch  {
  

  @RegisterTransformer
  private static class Render extends MethodTransformer {
    
    @Override
    public ASMMethod getMethod() {
      return TypesMc.Methods.BoatRenderer_render;
    }

    @Override
    public void transform(MethodNode main) {
      
      InsnList insnList = new InsnList();
      
      insnList.add(new VarInsnNode(ALOAD, 1)); // load the boat entity
      insnList.add(new VarInsnNode(FLOAD, 2)); // load the boat yaw
      insnList.add(ASMHelper.call(INVOKESTATIC, TypesHook.Methods.ForgeHaxHooks_onRenderBoat));
      // fire the event and get the value(player rotationYaw) returned by the method in
      // ForgeHaxHooks
      insnList.add(new VarInsnNode(FSTORE, 2)); // store it in entityYaw
      
      main.instructions.insert(insnList); // insert code at the top of the method
    }
  }
}
