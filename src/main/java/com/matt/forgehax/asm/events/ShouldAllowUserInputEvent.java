package com.matt.forgehax.asm.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class ShouldAllowUserInputEvent extends Event {
    private final GuiScreen currentScreen;

    public ShouldAllowUserInputEvent() {
        this.currentScreen = Minecraft.getMinecraft().currentScreen;
    }

    public GuiScreen getCurrentScreen() {
        return currentScreen;
    }
}
