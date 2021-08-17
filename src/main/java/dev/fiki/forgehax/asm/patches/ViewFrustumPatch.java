package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.asm.MapClass;
import dev.fiki.forgehax.api.asm.MapMethod;
import dev.fiki.forgehax.asm.hooks.MarkerHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

@MapClass(ViewFrustum.class)
public class ViewFrustumPatch extends Patch {

  @Inject
  @MapMethod("createChunks")
  public void createChunks(MethodNode node,
      @MapMethod(parentClass = MarkerHooks.class, name = "onCreateRenderChunks") ASMMethod onCreateRenderChunks) {
    AbstractInsnNode ret = ASMHelper.findReturn(RETURN, node);

    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(ASMHelper.call(INVOKESTATIC, onCreateRenderChunks));

    node.instructions.insertBefore(ret, list);
  }

  @Inject
  @MapMethod("repositionCamera")
  public void repositionCamera(MethodNode node,
      @MapMethod(parentClass = ChunkRenderDispatcher.ChunkRender.class, name = "setOrigin") ASMMethod setOrigin,
      @MapMethod(parentClass = MarkerHooks.class, name = "onUpdateChunkPosition") ASMMethod onUpdateChunkPosition) {
    AbstractInsnNode call = ASMPattern.builder()
        .codeOnly()
        .custom(an -> setOrigin.matchesInvoke(INVOKEVIRTUAL, an))
        .find(node)
        .getFirst("Could not find call to setPosition");

    AbstractInsnNode invokeSpecial = ASMHelper.matchPrevious(call, an -> an.getOpcode() == INVOKESPECIAL)
        .orElseThrow(() -> new Error("Could not find INVOKESPECIAL"));

    VarInsnNode z = (VarInsnNode) call.getPrevious();
    VarInsnNode y = (VarInsnNode) z.getPrevious();
    VarInsnNode x = (VarInsnNode) y.getPrevious();

    VarInsnNode iz = (VarInsnNode) invokeSpecial.getPrevious();
    VarInsnNode iy = (VarInsnNode) iz.getPrevious();
    VarInsnNode ix = (VarInsnNode) iy.getPrevious();

    InsnList list = new InsnList();
    list.add(new VarInsnNode(ILOAD, ix.var));
    list.add(new VarInsnNode(ILOAD, iy.var));
    list.add(new VarInsnNode(ILOAD, iz.var));
    list.add(new VarInsnNode(ILOAD, x.var));
    list.add(new VarInsnNode(ILOAD, y.var));
    list.add(new VarInsnNode(ILOAD, z.var));
    list.add(ASMHelper.call(INVOKESTATIC, onUpdateChunkPosition));

    node.instructions.insert(call, list);
  }
}
