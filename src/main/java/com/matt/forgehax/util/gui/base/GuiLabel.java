package com.matt.forgehax.util.gui.base;

import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.gui.IGuiLabel;
import com.matt.forgehax.util.gui.IGuiParent;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;

/**
 * Created on 9/10/2017 by fr1kin
 */
public class GuiLabel extends GuiBase implements IGuiLabel {
    private String text = "";

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void onParentChanged(IGuiParent parent) {
        setWidth(SurfaceHelper.getStringWidth(getFontRenderer(), getText()));
        setHeight(SurfaceHelper.getStringHeight(getFontRenderer()));
    }

    @Override
    public void onRender(GuiRenderEvent event) {
        SurfaceHelper.drawString(getFontRenderer(), getText(), getX(), getY(), getFontColor(), false);
    }
}
