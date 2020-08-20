package dev.fiki.forgehax.asm.patches;

import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.MappedFormat;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.hooks.ForgeHaxHooks;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.asm.utils.ASMPattern;
import dev.fiki.forgehax.asm.utils.InsnPattern;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.asm.utils.transforming.Inject;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import net.minecraft.client.gui.overlay.PlayerTabOverlayGui;
import org.objectweb.asm.tree.*;

/**
 * Created by Babbaj on 8/9/2017. thanks 086 :3
 */

@ClassMapping(PlayerTabOverlayGui.class)
public class PlayerTabOverlayPatch extends Patch {
  @Inject
  @MethodMapping(value = "func_238523_a_", format = MappedFormat.SRG)
  public void render(MethodNode main,
      @MethodMapping(
          parentClass = ForgeHaxHooks.class,
          value = "shouldIncreaseTabListSize",
          args = {},
          ret = boolean.class
      ) ASMMethod hook) {
    InsnPattern nodes = ASMPattern.builder()
        .codeOnly()
        .opcodes(ALOAD,
            ICONST_0,
            ALOAD,
            INVOKEINTERFACE,
            BIPUSH,
            INVOKESTATIC,
            INVOKEINTERFACE,
            ASTORE)
        .find(main);

    AbstractInsnNode subListNode = nodes.getFirst();
    AbstractInsnNode post = nodes.getLast();

    LabelNode jump = new LabelNode();

    InsnList insnList = new InsnList();
    insnList.add(ASMHelper.call(INVOKESTATIC, hook));
    insnList.add(new JumpInsnNode(IFNE, jump));

    main.instructions.insertBefore(subListNode, insnList);
    main.instructions.insert(post, jump);
  }
}
