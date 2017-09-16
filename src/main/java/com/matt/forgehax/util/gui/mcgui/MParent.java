package com.matt.forgehax.util.gui.mcgui;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.gui.IGuiBase;
import com.matt.forgehax.util.gui.IGuiParent;
import com.matt.forgehax.util.gui.events.GuiKeyEvent;
import com.matt.forgehax.util.gui.events.GuiMouseEvent;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;
import com.matt.forgehax.util.gui.events.GuiUpdateEvent;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created on 9/15/2017 by fr1kin
 */
public class MParent extends MBase implements IGuiParent {
    protected final List<IGuiBase> children = Lists.newCopyOnWriteArrayList();

    @Override
    public void addChild(IGuiBase element) {
        if(!children.contains(element) && children.add(element)) {
            element.setParent(this);
            callbacks.forEach(cb -> cb.onChildAdded(element));
        }
    }

    @Override
    public void removeChild(IGuiBase element) {
        if(children.remove(element)) {
            element.setParent(null);
            callbacks.forEach(cb -> cb.onChildRemoved(element));
        }
    }

    @Override
    public void removeAllChildren() {
        children.forEach(this::removeChild);
    }

    @Override
    public List<IGuiBase> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public int getChildrenCount() {
        return children.size();
    }

    @Override
    public boolean focus(IGuiBase element) {
        if(this == element.getParent()
                && element != getChildInFocus()) {
            children.remove(element); // remove from list
            children.add(0, element); // readd at head of the stack
            return true;
        } else return false;
    }

    @Nullable
    @Override
    public IGuiBase getChildInFocus() {
        return !children.isEmpty() ? children.get(0) : null;
    }

    @Override
    public void onRenderChildren(GuiRenderEvent event) {
        for(int i = children.size() - 1; i >= 0; --i) { // since rendering last = top, we must iterate the list backwards for rendering
            IGuiBase gui = children.get(i);
            if(gui.isVisible()) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(getX(), getY(), 0.D);

                gui.onRender(event);

                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public void init(double screenWidth, double screenHeight) {
        super.init(screenWidth, screenHeight);

        for(IGuiBase gui : children) if(gui.isVisible()) {
            gui.init(screenWidth, screenHeight);
        }
    }

    @Override
    public void onUpdateSize() {
        super.onUpdateSize();
        for(IGuiBase gui : children) if(gui.isVisible()) {
            gui.onUpdateSize();
        }
    }

    @Override
    public void onMouseEvent(GuiMouseEvent event) {
        super.onMouseEvent(event);
        if(this.isInFocus()) {
            for (IGuiBase gui : children)
                if (gui.isVisible()) {
                    gui.onMouseEvent(event);
                }
        }
    }

    @Override
    public void onKeyEvent(GuiKeyEvent event) {
        super.onKeyEvent(event);
        if(this.isInFocus()) {
            for (IGuiBase gui : children)
                if (gui.isVisible()) {
                    gui.onKeyEvent(event);
                }
        }
    }

    @Override
    public void onUpdate(GuiUpdateEvent event) {
        super.onUpdate(event);

        // update children if visible
        for(IGuiBase gui : children) if(gui.isVisible())
            gui.onUpdate(event);
    }

    @Override
    public void onRender(GuiRenderEvent event) {
        // don't call super

        onRenderPreBackground(event);

        onRenderChildren(event);

        onRenderPostBackground(event);
    }
}
