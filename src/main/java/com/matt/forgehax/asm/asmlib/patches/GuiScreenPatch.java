package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.events.ReplacementHooks.GuiScreenEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.asm.utils.ASMHelper;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.objectweb.asm.Opcodes.*;

@Transformer(GuiScreen.class)
public class GuiScreenPatch {

    @Inject(name = "setWorldAndResolution", args = {Minecraft.class, int.class, int.class})
    public void initGuiHook(AsmMethod method) {
        final Consumer<GuiScreen> postFunc = gui -> ForgeHax.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Post(gui, FastReflection.Fields.GuiScreen_buttonList.get(gui)));

        @Nullable AbstractInsnNode forgePre = ASMHelper.findPattern(method.method.instructions.getFirst(), new int[] {
                GETSTATIC, NEW, DUP, ALOAD, ALOAD, GETFIELD, INVOKESPECIAL, INVOKEVIRTUAL, IFNE
        }, "xxxxxxxx?");
        if (forgePre != null) {// work with forge's event
            final JumpInsnNode jump = (JumpInsnNode)forgePre.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext();
            method.setCursor(jump);
            method.visitInsn(new VarInsnNode(ALOAD, 0)); // push this
            method.<Predicate<GuiScreen>>invoke(gui ->
                // TODO: get button list with asm
                ForgeHax.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Pre(gui, FastReflection.Fields.GuiScreen_buttonList.get(gui)))
            );
            method.visitInsn(new InsnNode(IOR)); // ensure both forge's event and our event are fired, jump if either one is canceled

            method.setCursor(jump.label);
            method.visitInsn(new VarInsnNode(ALOAD, 0)); // push this
            method.invoke(postFunc);
        } else {
            // not forge
            throw new Error();
        }
    }


}
