package com.matt.forgehax.util.gui.base;

import com.matt.forgehax.util.gui.IGuiCheckbox;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;

/**
 * Created on 9/9/2017 by fr1kin
 */
public class GuiCheckbox extends GuiBase implements IGuiCheckbox {
    private String text = "";
    private String hoverText = "";

    private boolean checked = false;

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
        onResizeNeeded();
    }

    @Override
    public String getHoverText() {
        return hoverText;
    }

    @Override
    public void setHoverText(String text) {
        this.hoverText = text;
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void setChecked(boolean checked) {
        if(checked != this.checked) {
            this.checked = checked;
            onCheckChanged();
        }
    }

    @Override
    public void onCheckChanged() {}

    @Override
    public void onRender(GuiRenderEvent event) {
        
    }
}
