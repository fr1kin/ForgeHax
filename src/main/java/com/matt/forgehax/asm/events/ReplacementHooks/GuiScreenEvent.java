package com.matt.forgehax.asm.events.ReplacementHooks;

import com.matt.forgehax.util.event.Cancelable;
import com.matt.forgehax.util.event.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.List;

public class GuiScreenEvent extends Event {
    private final GuiScreen gui;

    public GuiScreenEvent(GuiScreen gui)
    {
        this.gui = gui;
    }

    public GuiScreen getGui()
    {
        return gui;
    }

    public static class InitGuiEvent extends GuiScreenEvent
    {
        private final List<GuiButton> buttonList;

        public InitGuiEvent(GuiScreen gui, List<GuiButton> buttonList)
        {
            super(gui);
            this.buttonList = buttonList;
        }


        public List<GuiButton> getButtonList()
        {
            return buttonList;
        }


        public static class Pre extends InitGuiEvent implements Cancelable
        {
            public Pre(GuiScreen gui, List<GuiButton> buttonList)
            {
                super(gui, buttonList);
            }
        }

        public static class Post extends InitGuiEvent
        {
            public Post(GuiScreen gui, List<GuiButton> buttonList)
            {
                super(gui, buttonList);
            }
        }
    }


}
