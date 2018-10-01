package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.events.replacementhooks.ClientTickEvent;
import com.matt.forgehax.asm.events.replacementhooks.GuiOpenEvent;
import com.matt.forgehax.asm.events.replacementhooks.InputEvent;
import com.matt.forgehax.asm.events.replacementhooks.WorldEvent;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.util.event.Event;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import org.objectweb.asm.tree.*;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.matt.forgehax.Globals.MC;
import static org.objectweb.asm.Opcodes.*;

@Transformer(Minecraft.class)
public class MinecraftPatch {

    @Inject(name = "init",
    description = "Inject at init for forgehax initialization")
    public void init(AsmMethod method) {
        final AbstractInsnNode setEffectRenderer = method.stream()
                .filter(insn -> insn.getOpcode() == PUTFIELD)
                .filter(insn -> ((FieldInsnNode)insn).name.matches("j|effectRenderer"))// TODO: get runtime name from mcp name from asmlib
                .findFirst()
                .orElseGet(() -> {
                    System.err.println("Failed to find proper injection point for forgehax initialization, falling back to RETURN");
                    return getReturnNode(method);
                });
        Objects.requireNonNull(setEffectRenderer, "Failed to find injection point for initialization");

        method.setCursor(setEffectRenderer.getNext());
        method.run(ForgeHax::init);
    }

    @Inject(name = "runTickMouse",
    description = "Add hook to for mouse input event")
    public void mouseHook(AsmMethod method) {
        AbstractInsnNode node = ASMHelper.findPattern(method.method.instructions.getFirst(), new int[] {
                LLOAD, LDC, LCMP, IFGT
        }, "xxxx"); // if (j <= 200)
        Objects.requireNonNull(node);
        JumpInsnNode jump = (JumpInsnNode)node.getNext().getNext().getNext();
        method.setCursor(jump.label);
        method.run(() -> ForgeHax.EVENT_BUS.post(new InputEvent.MouseInputEvent()));
    }

    @Inject(name = "runTickKeyboard",
    description = "Add hook for keyboard input event")
    public void keyboardHook(AsmMethod method) {
        AbstractInsnNode node = ASMHelper.findPattern(method.method.instructions.getFirst(), new int[] {
                0x00, 0x00, 0x00, INVOKESTATIC, IFEQ // first opcode is the label before the while loop
        }, "???xx"); // while(Keyboard.next())
        Objects.requireNonNull(node);
        final LabelNode start = (LabelNode)node;
        final AbstractInsnNode gotoNode = method.stream()
                .filter(insn -> insn.getOpcode() == GOTO)
                .filter(insn -> ((JumpInsnNode)insn).label == start)
                .reduce((a, b) -> b) // get last
                .get();
        method.setCursor(gotoNode);
        method.run(() -> ForgeHax.EVENT_BUS.post(new InputEvent.KeyInputEvent()));
    }

    @Inject(name = "runTick",
    description = "Add hook for tick event") // TODO: post
    public void tickHook(AsmMethod method) {
        method.run(() -> ForgeHax.EVENT_BUS.post(new ClientTickEvent()));
    }

    @Inject(name = "displayGuiScreen", args = {GuiScreen.class},
    description = "Add hook for when a gui is displayed")
    public void displayGuiHook(AsmMethod method) {
        AbstractInsnNode node = ASMHelper.findPattern(method.method.instructions.getFirst(), new int[] {
            ALOAD, GETFIELD, ASTORE
        }, "xxx"); // GuiScreen old = this.currentScreen;
        Objects.requireNonNull(node);
        method.setCursor(node);
        method.visitInsn(new VarInsnNode(ALOAD, 1));
        method.<Function<GuiScreen, GuiOpenEvent>>invoke(newGui -> {
            GuiOpenEvent event = new GuiOpenEvent(newGui);
            ForgeHax.EVENT_BUS.post(event);
            return event;
        });
        method.visitInsn(new InsnNode(DUP)); // 2 event objects now on the stack
        method.<Predicate<GuiOpenEvent>>invoke(Event::isCanceled);
        method.returnIf(true);
        method.apply(GuiOpenEvent::getGui);
        method.visitInsn(new VarInsnNode(ASTORE, 1));
    }

    @Inject(name = "loadWorld", args = {WorldClient.class, String.class})
    public void loadWorldHook(AsmMethod method) {
        method.run(() -> {
            if (MC.world != null) {
                ForgeHax.EVENT_BUS.post(new WorldEvent.UnLoad(MC.world));
            }
        });
    }

    private static AbstractInsnNode getReturnNode(AsmMethod method) {
        return method.stream()
                .filter(insn -> insn.getOpcode() == RETURN)
                .findFirst()
                .orElse(null);
    }
}
