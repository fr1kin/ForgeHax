package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.events.replacementhooks.RenderTooltipEvent;
import com.matt.forgehax.asm.events.replacementhooks.GuiScreenEvent;
import com.matt.forgehax.asm.events.replacementhooks.GuiScreenEvent.*;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.InsnPattern;
import com.matt.forgehax.util.event.Event;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.*;

import static com.matt.forgehax.Globals.MC;
import static org.objectweb.asm.Opcodes.*;

@Transformer(GuiScreen.class)
public class GuiScreenPatch {

    private static List<GuiButton> getButtonList(GuiScreen gui) {
        return FastReflection.Fields.GuiScreen_buttonList.get(gui);
    }

    @Inject(name = "setWorldAndResolution", args = {Minecraft.class, int.class, int.class})
    public void initGuiHook(AsmMethod method) {
        // TODO: get button list with asm
        final Consumer<GuiScreen>  postFunc = gui -> ForgeHax.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Post(gui, getButtonList(gui)));
        final Predicate<GuiScreen> preFunc =  gui -> ForgeHax.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Pre (gui, getButtonList(gui)));

        @Nullable AbstractInsnNode forgePre = ASMHelper.findPattern(method.method.instructions.getFirst(), new int[] {
                GETSTATIC, NEW, DUP, ALOAD, ALOAD, GETFIELD, INVOKESPECIAL, INVOKEVIRTUAL, IFNE
        }, "xxxxxxxx?");
        if (forgePre != null) {// work with forge's event
            final JumpInsnNode jump = (JumpInsnNode)forgePre.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext();
            method.setCursor(jump);
            method.visitInsn(new VarInsnNode(ALOAD, 0)); // push this
            method.<Predicate<GuiScreen>>invoke(preFunc);
            method.visitInsn(new InsnNode(IOR)); // ensure both forge's event and our event are fired, jump if either one is canceled

            method.setCursor(jump.label);
            method.visitInsn(new VarInsnNode(ALOAD, 0)); // push this
            method.invoke(postFunc);

        } else {// not forge
            final AbstractInsnNode start = ASMHelper.findPattern(method.method.instructions.getFirst(), new int[] {
                    ALOAD, GETFIELD, INVOKEINTERFACE, 0x00, 0x00, ALOAD, INVOKEVIRTUAL
            }, "xxx??xx");
            final AbstractInsnNode end = ASMHelper.findPattern(method.method.instructions.getFirst(), new int[] {
                    RETURN
            }, "x");

            method.setCursor(start);
            method.visitInsn(new VarInsnNode(ALOAD, 0)); // push this
            method.invoke(preFunc);
            method.jumpIf(IFNE, end);
            method.setCursor(end);
            method.visitInsn(new VarInsnNode(ALOAD, 0)); // push this
            method.invoke(postFunc);

        }
    }

    @Inject(name = "mouseClicked", args = {int.class, int.class, int.class})
    public void mouseClickedHook(AsmMethod method) {
        InsnPattern ifMousePressed = ASMHelper._findPattern(method.method.instructions.getFirst(), new int[] {
                ALOAD, ALOAD, GETFIELD, ILOAD, ILOAD, INVOKEVIRTUAL, IFEQ
        }, "xxxxxxx");
        Objects.requireNonNull(ifMousePressed, "Failed to find ifMousePressed node");

        final int GUIBUTTON_INDEX = 5;

        final JumpInsnNode jumpNode = ifMousePressed.getLast();

        final AbstractInsnNode start = jumpNode.getNext();
        final AbstractInsnNode end = jumpNode.label;

        { // pre
            method.setCursor(start);
            method.visitInsn(new VarInsnNode(ALOAD, 0)); // this
            method.visitInsn(new VarInsnNode(ALOAD, GUIBUTTON_INDEX)); // guiButton
            method.<BiFunction<GuiScreen, GuiButton, GuiScreenEvent.ActionPerformedEvent.Pre>>invoke((gui, button) ->
                    new GuiScreenEvent.ActionPerformedEvent.Pre(gui, button, getButtonList(gui))
            );
            method.visitInsn(new InsnNode(DUP));
            method.visitInsn(new InsnNode(DUP)); // 3 event objects on the stack
            method.<Consumer<GuiScreenEvent.ActionPerformedEvent.Pre>>invoke(gui -> ForgeHax.EVENT_BUS.post(gui));
            method.<Predicate<Event>>invoke(Event::isCanceled);
            method.returnIf(true);


            method.<Function<GuiScreenEvent.ActionPerformedEvent.Pre, GuiButton>>invoke(ActionPerformedEvent::getButton);
            method.visitInsn(new VarInsnNode(ASTORE, GUIBUTTON_INDEX)); // store in guiButton
        }

        { // post
            method.setCursor(end);
            method.visitInsn(new VarInsnNode(ALOAD, 0)); // this
            method.visitInsn(new VarInsnNode(ALOAD, GUIBUTTON_INDEX));
            method.<BiConsumer<GuiScreen, GuiButton>>invoke((gui, button) -> {
                if (gui == MC.currentScreen) {
                    ForgeHax.EVENT_BUS.post(new ActionPerformedEvent.Post(gui, button, getButtonList(gui)));
                }
            });
        }

    }

    @Inject(name = "drawDefaultBackground")
    public void drawBackgroundHook(AsmMethod method) {
        AbstractInsnNode ret = ASMHelper.findPattern(method.method.instructions.getFirst(), new int[] {
                RETURN
        }, "x");

        method.setCursor(ret);
        method.visitInsn(new VarInsnNode(ALOAD, 0)); // this
        method.<GuiScreen>consume(gui -> {
            ForgeHax.EVENT_BUS.post(new GuiScreenEvent.BackgroundDrawnEvent(gui));
        });
    }

    @Inject(name = "handleInput")
    public void inputHook(AsmMethod method) {
        InsnPattern mouseWhile = ASMHelper._findPattern(method.method.instructions.getFirst(), new int[] {
                0x00, 0x00, 0x00, INVOKESTATIC, IFEQ
        }, "???xx");
        Objects.requireNonNull(mouseWhile);
        InsnPattern keyboardWhile = ASMHelper._findPattern(mouseWhile.getLast(), new int[] {
                0x00, 0x00, 0x00, INVOKESTATIC, IFEQ
        }, "???xx");
        Objects.requireNonNull(keyboardWhile);

        doInputHook(method, mouseWhile, gui ->
                ForgeHax.EVENT_BUS.post(new GuiScreenEvent.MouseInputEvent.Pre(gui))
        );
        doInputHook(method, keyboardWhile, gui ->
                ForgeHax.EVENT_BUS.post(new GuiScreenEvent.KeyboardInputEvent.Pre(gui))
        );
    }
    private void doInputHook(AsmMethod method, InsnPattern whilePattern, Predicate<GuiScreen> eventFunc) {
        final LabelNode label = whilePattern.getFirst();
        method.setCursor(whilePattern.getLast().getNext());
        method.visitInsn(new VarInsnNode(ALOAD, 0));
        method.invoke(eventFunc);
        method.visitInsn(new JumpInsnNode(IFNE, label)); // continue;
    }


    public static ItemStack cachedToolTip; //= ItemStack.EMPTY;

    // TODO: set local variables from event
    @Inject(name = "drawHoveringText", args = {List.class, int.class, int.class, FontRenderer.class},
            description = "Add hook for tool tip event")
    public void drawHoveringTextHook(AsmMethod method) {
        InsnPattern node = ASMHelper._findPattern(method.method.instructions.getFirst(), new int[] {
                ALOAD, INVOKEINTERFACE, IFNE
        }, "xxx");
        if (node == null) {
            System.out.println("Failed to find pattern for drawHoveringText, probably in forge environment");
            return;
        }
        doDrawHoveringTextHook(method, Arrays.asList(
                new VarInsnNode(ALOAD, 1),
                new VarInsnNode(ILOAD, 2),
                new VarInsnNode(ILOAD, 3),
                new VarInsnNode(ALOAD, 4)
        ));

    }
    public static void doDrawHoveringTextHook(AsmMethod method, List<VarInsnNode> args) {
        InsnPattern node = ASMHelper._findPattern(method.method.instructions.getFirst(), new int[] {
                ALOAD, INVOKEINTERFACE, IFNE
        }, "xxx");
        Objects.requireNonNull(node);

        method.setCursor(node.getLast().getNext());
        args.forEach(method::visitInsn);
        method.<HoverTextPredicate>invoke((lines, mouseX, mouseY, fr) ->
            ForgeHax.EVENT_BUS.post(new RenderTooltipEvent(cachedToolTip != null ? cachedToolTip : ItemStack.EMPTY, lines, mouseX, mouseY, fr))
        );
        method.returnIf(true);
    }

    private interface HoverTextPredicate {
        boolean test(List<String> lines, int x, int y, FontRenderer fr);
    }

    @Inject(name = "renderToolTip", args = {ItemStack.class, int.class, int.class})
    public void renderToolTipHook(AsmMethod method) {
        method.visitInsn(new VarInsnNode(ALOAD, 1)); // stack
        method.<ItemStack>consume(stack -> cachedToolTip = stack);

        AbstractInsnNode ret = ASMHelper.findPattern(method.method.instructions.getFirst(), new int[] {
                RETURN
        }, "x");
        method.setCursor(ret);
        method.run(() -> cachedToolTip = ItemStack.EMPTY);
    }


}
