package com.matt.forgehax.gui.windows;

import com.matt.forgehax.gui.ClickGui;
import com.matt.forgehax.gui.elements.GuiButton;
import com.matt.forgehax.util.mod.Category;

import java.util.ArrayList;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class GuiWindowMod extends GuiWindow {

    public ArrayList<GuiButton> buttons = new ArrayList<GuiButton>();

    int buttonListOffset; // used for scrolling

    public Category category;

    public GuiWindowMod(Category categoryIn) {
        super(categoryIn);
        category = categoryIn;
    }



}
