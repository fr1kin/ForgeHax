package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
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

@ClassMapping(ViewFrustum.class)
public class ViewFrustumPatch extends Patch {

  @Inject
  @MethodMapping("createRenderChunks")
  public void createRenderChunks(MethodNode node,
      @MethodMapping(
          parentClass = MarkerHooks.class,
          value = "onCreateRenderChunks",
          args = {ViewFrustum.class},
          ret = void.class
      ) ASMMethod onCreateRenderChunks) {
    AbstractInsnNode ret = ASMHelper.findReturn(RETURN, node);

    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(ASMHelper.call(INVOKESTATIC, onCreateRenderChunks));

    node.instructions.insertBefore(ret, list);
  }

  @Inject
  @MethodMapping("updateChunkPositions")
  public void updateChunkPositions(MethodNode node,
      @MethodMapping(
          parentClass = ChunkRenderDispatcher.ChunkRender.class,
          value = "setPosition"
      ) ASMMethod setPosition,
      @MethodMapping(
          parentClass = MarkerHooks.class,
          value = "onUpdateChunkPosition",
          args = {int.class, int.class, int.class, int.class, int.class, int.class},
          ret = void.class
      ) ASMMethod onUpdateChunkPosition) {
    AbstractInsnNode call = ASMPattern.builder()
        .codeOnly()
        .custom(an -> setPosition.matchesInvoke(INVOKEVIRTUAL, an))
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
