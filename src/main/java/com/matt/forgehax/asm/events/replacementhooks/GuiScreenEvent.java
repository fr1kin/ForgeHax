package com.matt.forgehax.asm.events.replacementhooks;

import com.matt.forgehax.util.event.Cancelable;
import com.matt.forgehax.util.event.Event;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

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

    public static class ActionPerformedEvent extends GuiScreenEvent
    {
        private GuiButton button;
        private final List<GuiButton> buttonList;

        public ActionPerformedEvent(GuiScreen gui, GuiButton button, List<GuiButton> buttonList)
        {
            super(gui);
            this.button = button;
            this.buttonList = buttonList;
        }

        // will change what button actionPerformed is called with
        public void setButton(GuiButton button) {
            this.button = button;
        }

        public GuiButton getButton() {
            return this.button;
        }

        public List<GuiButton> getButtonList()
        {
            return buttonList;
        }


        public static class Pre extends ActionPerformedEvent implements Cancelable
        {
            public Pre(GuiScreen gui, GuiButton button, List<GuiButton> buttonList)
            {
                super(gui, button, buttonList);
            }
        }

        public static class Post extends ActionPerformedEvent
        {
            public Post(GuiScreen gui, GuiButton button, List<GuiButton> buttonList)
            {
                super(gui, button, buttonList);
            }
        }
    }

    public static class DrawScreenEvent extends GuiScreenEvent
    {
        private final int mouseX;
        private final int mouseY;
        private final float renderPartialTicks;

        public DrawScreenEvent(GuiScreen gui, int mouseX, int mouseY, float renderPartialTicks)
        {
            super(gui);
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.renderPartialTicks = renderPartialTicks;
        }


        public int getMouseX()
        {
            return mouseX;
        }


        public int getMouseY()
        {
            return mouseY;
        }


        public float getRenderPartialTicks()
        {
            return renderPartialTicks;
        }


        public static class Pre extends DrawScreenEvent implements Cancelable
        {
            public Pre(GuiScreen gui, int mouseX, int mouseY, float renderPartialTicks)
            {
                super(gui, mouseX, mouseY, renderPartialTicks);
            }
        }


        public static class Post extends DrawScreenEvent
        {
            public Post(GuiScreen gui, int mouseX, int mouseY, float renderPartialTicks)
            {
                super(gui, mouseX, mouseY, renderPartialTicks);
            }
        }
    }

    public static class BackgroundDrawnEvent extends GuiScreenEvent
    {
        private final int mouseX;
        private final int mouseY;

        public BackgroundDrawnEvent(GuiScreen gui)
        {
            super(gui);
            final ScaledResolution scaledresolution = new ScaledResolution(gui.mc);
            final int scaledWidth = scaledresolution.getScaledWidth();
            final int scaledHeight = scaledresolution.getScaledHeight();
            this.mouseX = Mouse.getX() * scaledWidth / gui.mc.displayWidth;
            this.mouseY = scaledHeight - Mouse.getY() * scaledHeight / gui.mc.displayHeight - 1;
        }


        public int getMouseX()
        {
            return mouseX;
        }


        public int getMouseY()
        {
            return mouseY;
        }
    }

    public static class MouseInputEvent extends GuiScreenEvent
    {
        public MouseInputEvent(GuiScreen gui)
        {
            super(gui);
        }

        public static class Pre extends MouseInputEvent implements Cancelable
        {
            public Pre(GuiScreen gui)
            {
                super(gui);
            }
        }

        // TODO:
        /*public static class Post extends MouseInputEvent
        {
            public Post(GuiScreen gui)
            {
                super(gui);
            }
        }*/
    }

    public static class KeyboardInputEvent extends GuiScreenEvent
    {
        public KeyboardInputEvent(GuiScreen gui)
        {
            super(gui);
        }

        public static class Pre extends KeyboardInputEvent implements Cancelable
        {
            public Pre(GuiScreen gui)
            {
                super(gui);
            }
        }

        // TODO:
        /*public static class Post extends KeyboardInputEvent
        {
            public Post(GuiScreen gui)
            {
                super(gui);
            }
        }*/
    }

}
