package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.hooks.MarkerHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

@ClassMapping(ChunkRenderDispatcher.ChunkRender.class)
public class ChunkRenderDispatcher$ChunkRenderPatch extends Patch {

  @Inject
  @MethodMapping("rebuildChunkLater")
  public void rebuildChunkLater(MethodNode node,
      @MethodMapping(
          parentClass = MarkerHooks.class,
          value = "onRebuildChunk",
          args = {ChunkRenderDispatcher.ChunkRender.class, boolean.class},
          ret = void.class
      ) ASMMethod onRebuildChunk) {
    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(new InsnNode(ICONST_1));
    list.add(ASMHelper.call(INVOKESTATIC, onRebuildChunk));

    node.instructions.insert(list);
  }

  @Inject
  @MethodMapping("rebuildChunk")
  public void rebuildChunk(MethodNode node,
      @MethodMapping(
          parentClass = MarkerHooks.class,
          value = "onRebuildChunk",
          args = {ChunkRenderDispatcher.ChunkRender.class, boolean.class},
          ret = void.class
      ) ASMMethod onRebuildChunk) {
    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(new InsnNode(ICONST_0));
    list.add(ASMHelper.call(INVOKESTATIC, onRebuildChunk));

    node.instructions.insert(list);
  }
}
