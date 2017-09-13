package com.matt.forgehax.util.gui.base;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.gui.IGuiBase;
import com.matt.forgehax.util.gui.IGuiParent;
import com.matt.forgehax.util.gui.events.GuiKeyEvent;
import com.matt.forgehax.util.gui.events.GuiMouseEvent;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;
import com.matt.forgehax.util.gui.events.GuiUpdateEvent;
import uk.co.hexeption.thx.ttf.MinecraftFontRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Stack;

/**
 * Created on 9/9/2017 by fr1kin
 */
public class GuiBase implements IGuiBase {
    private double x = 0;
    private double y = 0;

    private double width = 0;
    private double height = 0;

    private boolean visible = true;

    private IGuiParent parent = null;

    private boolean hovered = false;
    private int hoveredTime = 0;

    private int focusTime = 0;

    private Stack<IGuiBase> focusStack = null;

    private MinecraftFontRenderer fontRenderer = null;
    private Integer fontColor = null;

    @Override
    public void init(double screenWidth, double screenHeight) {}

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setWidth(double w) {
        this.width = w;
    }

    @Override
    public void setHeight(double h) {
        this.height = h;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
        if(!visible) {
            focusTime = 0;
            hoveredTime = 0;
            hovered = false;
            unfocusHard();
        }
    }

    @Override
    public IGuiParent getParent() {
        return parent;
    }

    @Override
    public void setParent(IGuiParent parent) {
        if(this.parent == parent) return; // no need to continue
        if(parent == null && this.parent != null) { // remove parent and don't add another
            IGuiParent oldParent = this.parent;
            this.parent = null;
            oldParent.removeChild(this);
        } else {
            if(this.parent != null) { // remove from old parent
                IGuiParent oldParent = this.parent;
                this.parent = null;
                oldParent.removeChild(this);
            }
            this.parent = parent;
            this.parent.addChild(this);
        }
        onResizeNeeded();
    }

    @Override
    public boolean isHovered() {
        return hovered;
    }

    @Override
    public int getHoveredTime() {
        return hoveredTime;
    }

    @Override
    @Nonnull
    public Stack<IGuiBase> getFocusStack() {
        if(hasParent())
            return getParent().getFocusStack(); // get parents focus stack
        else {
            // if it has no parent, then create own focus stack
            if(this.focusStack == null) this.focusStack = new Stack<>();
            return this.focusStack;
        }
    }

    @Override
    public boolean isFocused() {
        return getFocusStack().contains(this);
    }

    @Override
    public boolean isTopFocused() {
        return !getFocusStack().isEmpty() && getFocusStack().peek() == this;
    }

    @Override
    public void focus() {
        if(!isTopFocused()) {
            getFocusStack().push(this);
            onFocusChanged(true);
        }
    }

    @Override
    public void unfocus() {
        if(isTopFocused()) {
            getFocusStack().pop();
            onFocusChanged(false);
        }
    }

    @Override
    public void unfocusHard() {
        unfocus();
        while(getFocusStack().contains(this)) getFocusStack().removeElement(this);
    }

    @Override
    public int getFocusTime() {
        return focusTime;
    }

    @Nullable
    @Override
    public MinecraftFontRenderer getFontRenderer() {
        return fontRenderer != null ? fontRenderer : (getParent() != null ? getParent().getFontRenderer() : null);
    }

    @Override
    public void setFontRenderer(MinecraftFontRenderer fontRenderer) {
        this.fontRenderer = fontRenderer;
        onResizeNeeded();
    }

    @Override
    public int getFontColor() {
        return fontColor != null ? fontColor : (getParent() != null ? getParent().getFontColor() : Utils.Colors.WHITE);
    }

    @Override
    public void setFontColor(int buffer) {
        this.fontColor = buffer;
    }

    @Override
    public void onResizeNeeded() {}

    @Override
    public void onFocusChanged(boolean state) {}

    @Override
    public void onMouseEvent(GuiMouseEvent event) {}

    @Override
    public void onKeyEvent(GuiKeyEvent event) {}

    @Override
    public void onUpdate(GuiUpdateEvent event) {
        // check if mouse is hovered over
        if(event.getMouseX() > getRealX()
                && event.getMouseX() < (getRealX() + getWidth())
                && event.getMouseY() > getRealY()
                && event.getMouseY() < (getRealY() + getHeight())) {
            this.hovered = true;
            this.hoveredTime++;
        } else {
            this.hovered = false;
            this.hoveredTime = 0;
        }

        // update focus time
        if(isTopFocused())
            this.focusTime++;
        else
            this.focusTime = 0;
    }

    @Override
    public void onRender(GuiRenderEvent event) {}

    @Override
    public void onRenderPreBackground(GuiRenderEvent event) {}

    @Override
    public void onRenderPostBackground(GuiRenderEvent event) {}
}
