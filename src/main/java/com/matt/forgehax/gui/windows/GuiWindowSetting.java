package com.matt.forgehax.gui.windows;

import com.matt.forgehax.gui.ClickGui;
import com.matt.forgehax.gui.elements.GuiElement;
import com.matt.forgehax.gui.elements.GuiTextInput;
import com.matt.forgehax.util.mod.BaseMod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class GuiWindowSetting extends GuiWindow {

    public List<GuiElement> inputList = new ArrayList<>(); // list of toggles, sliders, text inputs, etc.

    private BaseMod mod;

    public GuiWindowSetting(BaseMod modIn, int x, int y) {
        super(modIn.getModName() + " Settings");
        this.mod = modIn;
        initializeInputs();
    }

    private void initializeInputs() {
        //Map<Object, String> settingMap;
        //inputList.add(new GuiSlider());
        inputList.add(new GuiTextInput(null,this));
        height += 13;
    }

    public String getModName() {
        return mod.getModName();
    }

    public BaseMod getMod() {
        return this.mod;
    }

    public void drawWindow(int mouseX, int mouseY) {
        super.drawWindow(mouseX, mouseY);

        for (GuiElement input : inputList) {
            input.x = 2;
            input.y = height+2;
            input.width = width;
            input.draw(mouseX, mouseY);
        }

        //update variables
        bottomX = posX + width; // set the coords of the bottom right corner for mouse coord testing
        bottomY = windowY + height;
    }

    public void keyTyped(char typedChar, int keyCode) throws IOException {
        for (GuiElement element : inputList) {
            element.keyTyped(typedChar, keyCode);
        }
    }

    public void mouseClicked(int x, int y, int state) {
        super.mouseClicked(x, y, state);
        if (state == 2 && isMouseInHeader(x,y))  { // delete the window on middle click
           ClickGui.getInstance().windowList.remove(this);
        }
        else {
            for (GuiElement input : inputList) {
                //if (isMouseInElement(x , y, input))
                input.mouseClicked(x, y, state);
            }
        }

    }

    public void mouseReleased(int x, int y, int state) {
        super.mouseReleased(x,y,state);
        for (GuiElement input : inputList) {
            input.mouseReleased(x, y, state);
        }
    }


}
