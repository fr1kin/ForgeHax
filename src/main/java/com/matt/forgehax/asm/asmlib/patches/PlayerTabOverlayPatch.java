package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.asm.TypesHook;
import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Transformer(GuiPlayerTabOverlay.class)
public class PlayerTabOverlayPatch {

    @Inject(name = "renderPlayerlist", args = {int.class, Scoreboard.class, ScoreObjective.class},
    description = "Add hook to increase the size of the tab list"
    )
    public void renderPlayerlist(MethodNode main) {
        AbstractInsnNode subListNode = ASMHelper.findPattern(main.instructions.getFirst(), new int[] {
                ALOAD, ICONST_0, ALOAD, INVOKEINTERFACE, BIPUSH, INVOKESTATIC, INVOKEINTERFACE, ASTORE
        }, "xxxxxxxx");

        AbstractInsnNode astoreNode = subListNode;
        for (int i = 0; i < 7; i++) {
            astoreNode = astoreNode.getNext();
        }

        Objects.requireNonNull(subListNode, "Find pattern failed for subList");
        Objects.requireNonNull(astoreNode, "Find pattern failed for subListPost");

        LabelNode jump = new LabelNode();

        InsnList insnList = new InsnList();
        insnList.add(ASMHelper.call(GETSTATIC, TypesHook.Fields.ForgeHaxHooks_doIncreaseTabListSize));
        insnList.add(new JumpInsnNode(IFNE, jump));

        main.instructions.insertBefore(subListNode, insnList);
        main.instructions.insert(astoreNode, jump);
    }

}
