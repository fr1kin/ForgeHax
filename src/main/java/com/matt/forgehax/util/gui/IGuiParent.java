package com.matt.forgehax.util.gui;

import com.matt.forgehax.util.gui.events.GuiRenderEvent;

import java.util.List;

/**
 * Created on 9/9/2017 by fr1kin
 */
public interface IGuiParent extends IGuiBase {
    void addChild(IGuiBase element);
    void removeChild(IGuiBase element);

    void removeAllChildren();

    List<IGuiBase> getChildren();
    int getChildrenCount();

    void onChildAdded(IGuiBase element);
    void onChildRemoved(IGuiBase element);

    void onRenderChildren(GuiRenderEvent event);
}
