package com.matt.forgehax.asm.events.replacementhooks;

import com.matt.forgehax.util.event.Cancelable;
import com.matt.forgehax.util.event.Event;
import net.minecraft.client.gui.GuiScreen;

public class GuiOpenEvent extends Event implements Cancelable {
    private GuiScreen gui;

    public GuiOpenEvent(GuiScreen gui)
    {
        this.gui = gui;
    }

    public GuiScreen getGui()
    {
        return gui;
    }

    public void setGui(GuiScreen gui)
    {
        this.gui = gui;
    }
}
