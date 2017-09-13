package com.matt.forgehax.util.gui.base;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.gui.IGuiBase;
import com.matt.forgehax.util.gui.IGuiPanel;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;
import com.matt.forgehax.util.gui.events.GuiUpdateEvent;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;

/**
 * Created on 9/9/2017 by fr1kin
 */
public class GuiPanel extends GuiBase implements IGuiPanel {
    protected List<IGuiBase> children = Lists.newArrayList();

    private boolean collapsed = false;

    private int maxRows = Integer.MAX_VALUE;
    private int maxColumns = 1;

    private boolean verticalScrolling = false;
    private boolean horizontalScrolling = false;

    @Override
    public boolean isCollapsed() {
        return collapsed;
    }

    @Override
    public void setCollapsed(boolean collapsed) {
        this.collapsed = true;
    }

    @Override
    public int getMaxRows() {
        return maxRows;
    }

    @Override
    public void setMaxRows(int rows) {
        this.maxRows = Math.max(1, rows);
    }

    @Override
    public int getMaxColumns() {
        return maxColumns;
    }

    @Override
    public void setMaxColumns(int columns) {
        this.maxColumns = Math.max(1, columns);
    }

    @Override
    public boolean isVerticalScrolling() {
        return verticalScrolling;
    }

    @Override
    public void setVerticalScrolling(boolean scrolling) {
        this.verticalScrolling = scrolling;
    }

    @Override
    public boolean isHorizontalScrolling() {
        return horizontalScrolling;
    }

    @Override
    public void setHorizontalScrolling(boolean scrolling) {
        this.horizontalScrolling = scrolling;
    }

    @Override
    public void addChild(IGuiBase element) {
        if(children.add(element)) {
            element.setParent(this);
            onChildAdded(element);
        }
    }

    @Override
    public void removeChild(IGuiBase element) {
        if(children.remove(element)) {
            element.setParent(null);
            onChildRemoved(element);
        }
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
    public void onChildAdded(IGuiBase element) {}

    @Override
    public void onChildRemoved(IGuiBase element) {}

    @Override
    public void onRenderChildren(GuiRenderEvent event) {
        GlStateManager.pushMatrix();

        GlStateManager.translate(getX(), getY(), 0.D);

        for(IGuiBase gui : children) if(gui.isVisible())
            gui.onRender(event);

        GlStateManager.popMatrix();
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
        super.onRender(event);

        onRenderPreBackground(event);

        onRenderChildren(event);

        onRenderPostBackground(event);
    }

    @Override
    public void onRenderPreBackground(GuiRenderEvent event) {
        super.onRenderPreBackground(event);

        BufferBuilder builder = event.getBuilder();

        int[] color = Utils.toRGBAArray(getFontColor());
        GlStateManager.color(color[0] / 255.f, color[1] / 255.f, color[2] / 255.f, color[3] / 255.f);

        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        SurfaceHelper.buildRectangle(event.getBuilder(), getX(), getY(), getWidth(), getHeight());

        event.getTessellator().draw();
    }
}
