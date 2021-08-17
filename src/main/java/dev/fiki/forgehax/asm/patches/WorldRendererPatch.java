package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.asm.MapClass;
import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.asm.MapMethod;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMField;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.client.renderer.WorldRenderer;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

@MapClass(WorldRenderer.class)
public class WorldRendererPatch extends Patch {

  @MapMethod("allChanged")
  public void allChanged(MethodNode node,
      @MapField("viewArea") ASMField viewFrustum) {
    AbstractInsnNode putViewFrustum = ASMPattern.builder()
        .codeOnly()
        .custom(an -> an.getOpcode() == PUTFIELD
            && an instanceof FieldInsnNode
            && viewFrustum.anyNameEquals(((FieldInsnNode) an).name))
        .find(node)
        .getFirst("Could not find PUTFIELD for viewFrustum!");
  }
}
