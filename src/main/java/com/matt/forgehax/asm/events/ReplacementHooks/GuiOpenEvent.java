package com.matt.forgehax.asm.events.ReplacementHooks;

import com.matt.forgehax.util.event.Cancelable;
import net.minecraft.client.gui.GuiScreen;

public class GuiOpenEvent extends Cancelable {
    private GuiScreen gui;
    public GuiOpenEvent(GuiScreen gui)
    {
        this.setGui(gui);
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
