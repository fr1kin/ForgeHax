package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.events.replacementhooks.GuiScreenEvent;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.InsnPattern;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.ForgeHooksClient;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Objects;

import static com.matt.forgehax.Globals.MC;
import static org.objectweb.asm.Opcodes.*;

@Transformer(target = "net.minecraftforge.client.ForgeHooksClient")
public class ForgeHooksClientPatch {

    @Inject(name = "drawScreen", args = {GuiScreen.class, int.class, int.class, float.class})
    public void drawScreenHook(AsmMethod method) {
        final AbstractInsnNode ret = ASMHelper.findPattern(method.method.instructions.getFirst(), new int[] {
                RETURN
        }, "x");
        final InsnPattern drawScreen = ASMHelper._findPattern(method.method.instructions.getFirst(), new int[] {
                ALOAD, ILOAD, ILOAD, FLOAD, INVOKEVIRTUAL
        }, "xxxxx");
        Objects.requireNonNull(drawScreen);

        method.visitInsn(new VarInsnNode(ILOAD, 1)); // mouseX
        method.visitInsn(new VarInsnNode(ILOAD, 2)); // mouseY
        method.<IntBiPredicate>invoke((mouseX, mouseY) ->
                ForgeHax.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Pre(
                        MC.currentScreen,
                        mouseX,
                        mouseY,
                        MC.getTickLength())
                )
        );
        method.jumpIf(IFNE, drawScreen.getLast().getNext());


        method.setCursor(ret);
        method.visitInsn(new VarInsnNode(ILOAD, 1)); // mouseX
        method.visitInsn(new VarInsnNode(ILOAD, 2)); // mouseY
        method.<IntBiConsumer>invoke((mouseX, mouseY) ->
                ForgeHax.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Post(
                        MC.currentScreen,
                        mouseX,
                        mouseY,
                        MC.getTickLength())
                )
        );
    }

    private interface IntBiPredicate {
        boolean test(int a, int b);
    }

    private interface IntBiConsumer {
        void accept(int a, int b);
    }
}
