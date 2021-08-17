package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.asm.MapClass;
import dev.fiki.forgehax.api.asm.MapMethod;
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

@MapClass(ChunkRenderDispatcher.ChunkRender.class)
public class ChunkRenderDispatcher$ChunkRenderPatch extends Patch {

  @Inject
  @MapMethod("rebuildChunkAsync")
  public void rebuildChunkAsync(MethodNode node,
      @MapMethod(parentClass = MarkerHooks.class, name = "onRebuildChunk") ASMMethod onRebuildChunk) {
    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(new InsnNode(ICONST_1));
    list.add(ASMHelper.call(INVOKESTATIC, onRebuildChunk));

    node.instructions.insert(list);
  }

  @Inject
  @MapMethod("compileSync")
  public void compileSync(MethodNode node,
      @MapMethod(parentClass = MarkerHooks.class, name = "onRebuildChunk") ASMMethod onRebuildChunk) {
    InsnList list = new InsnList();
    list.add(new VarInsnNode(ALOAD, 0));
    list.add(new InsnNode(ICONST_0));
    list.add(ASMHelper.call(INVOKESTATIC, onRebuildChunk));

    node.instructions.insert(list);
  }
}
