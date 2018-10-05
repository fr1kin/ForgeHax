package com.matt.forgehax.asm.asmlib.patches;

import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

@Transformer(GuiUtils.class)
public class Forge_GuiUtilsPatch {

    @Inject(name = "drawHoveringText", args = {ItemStack.class, List.class, int.class, int.class, int.class, int.class, int.class, FontRenderer.class},
    description = "Add hook for tool tip event")
    public void drawHoveringTextHook(AsmMethod method) {
        GuiScreenPatch.doDrawHoveringTextHook(method, Arrays.asList(
                new VarInsnNode(ALOAD, 1), // textLines
                new VarInsnNode(ILOAD, 2), // mouseX
                new VarInsnNode(ILOAD, 3), // mouseY
                new VarInsnNode(ALOAD, 7)  // font
        ));
    }
}
