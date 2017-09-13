package com.matt.forgehax.util.gui.base;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.gui.IGuiLabel;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

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
        onResizeNeeded();
    }

    @Override
    public void onResizeNeeded() {
        setWidth(SurfaceHelper.getStringWidth(getFontRenderer(), getText()));
        setHeight(SurfaceHelper.getStringHeight(getFontRenderer()));
    }

    @Override
    public void onRender(GuiRenderEvent event) {
        BufferBuilder builder = event.getBuilder();

        int[] color = Utils.toRGBAArray(Utils.toRGBA(255, 0, 0, 150));
        GlStateManager.color(color[0] / 255.f, color[1] / 255.f, color[2] / 255.f, color[3] / 255.f);

        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        SurfaceHelper.buildRectangle(event.getBuilder(), getX(), getY(), getWidth(), getHeight());

        event.getTessellator().draw();

        SurfaceHelper.drawString(getFontRenderer(), getText(), getX(), getY(), getFontColor(), false);
    }
}
