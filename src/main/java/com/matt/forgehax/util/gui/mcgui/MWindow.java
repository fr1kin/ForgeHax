package com.matt.forgehax.util.gui.mcgui;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.SurfaceBuilder;
import com.matt.forgehax.util.gui.GuiHelper;
import com.matt.forgehax.util.gui.IGuiWindow;
import com.matt.forgehax.util.gui.events.GuiMouseEvent;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;

/**
 * Created on 9/15/2017 by fr1kin
 */
public class MWindow extends MParent implements IGuiWindow {
    public static final double BAR_HEIGHT = 20;
    public static final double BUTTON_SIZE = 7;

    boolean closeable = true;
    boolean collapsible = true;
    boolean draggable = true;

    boolean collapsed = false;
    boolean dragging = false;

    double dragX = 0.D;
    double dragY = 0.D;

    private int backgroundColor = Utils.Colors.WHITE;

    @Override
    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
    }

    @Override
    public boolean isCloseable() {
        return closeable;
    }

    @Override
    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
    }

    @Override
    public boolean isCollapsible() {
        return collapsible;
    }

    @Override
    public void setCollapsible(boolean collapsible) {
        this.collapsible = collapsible;
    }

    @Override
    public boolean isCollapsed() {
        return collapsed;
    }

    @Override
    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    @Override
    public boolean isDraggable() {
        return draggable;
    }

    @Override
    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    @Override
    public void onClicked(GuiMouseEvent event) {
        requestFocus();
        super.onClicked(event);
    }

    @Override
    public void setHeight(double h) {
        super.setHeight(Math.max(h, BAR_HEIGHT));
    }

    @Override
    public void onFocusChanged() {
        super.onFocusChanged();

        if(!isInFocus()) {
            // focus lost
            dragging = false;
        }
    }

    @Override
    public void onMouseEvent(GuiMouseEvent event) {
        super.onMouseEvent(event);

        if(event.isLeftMouse()
                && GuiHelper.isInRectangle(event.getMouseX(), event.getMouseY(), getRealX(), getRealY(), getWidth(), BAR_HEIGHT)) {
            switch (event.getType()) {
                case PRESSED:
                    dragging = true;
                case DOWN: {
                    dragX = event.getMouseX() - getRealX();
                    dragY = event.getMouseY() - getRealY();
                    break;
                }
                case RELEASED:
                    dragging = false;
                    break;
            }
        }
    }

    @Override
    public void onRender(GuiRenderEvent event) {
        if(dragging) {
            setX(event.getMouseX() - dragX);
            setY(event.getMouseY() - dragY);
        }

        super.onRender(event);
    }

    @Override
    public void onRenderPreBackground(GuiRenderEvent event) {
        super.onRenderPreBackground(event);

        event.getSurfaceBuilder()
                .task(SurfaceBuilder::clearColor)
                .task(SurfaceBuilder::disableTexture2D)
                .task(SurfaceBuilder::enableBlend);

        // bar
        event.getSurfaceBuilder()
                .push()
                .color(getBackgroundColor())
                .beginQuads()
                .rectangle(getX(), getY(), getWidth(), BAR_HEIGHT)
                .end()
                .color(0, 0, 0, 255)
                .beginLineLoop()
                .rectangle(getX(), getY(), getWidth(), BAR_HEIGHT)
                .end()
                .pop();

        // bar headings
        if(isCloseable() || isCollapsible()) {

        }

        // actual window
        if(getHeight() > BAR_HEIGHT && !isCollapsed()) {
            event.getSurfaceBuilder()
                    .push()
                    .color(getBackgroundColor())
                    .beginQuads()
                    .rectangle(getX(), getY() + BAR_HEIGHT, getWidth(), getHeight() - BAR_HEIGHT)
                    .end()
                    .color(0, 0, 0, 255)
                    .beginLineLoop()
                    .rectangle(getX(), getY() + BAR_HEIGHT, getWidth(), getHeight() - BAR_HEIGHT)
                    .end()
                    .pop();
        }

        event.getSurfaceBuilder()
                .task(SurfaceBuilder::disableFontRendering)
                .task(SurfaceBuilder::disableBlend);
    }
}
