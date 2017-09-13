package com.matt.forgehax.util.gui.base;

import com.matt.forgehax.util.draw.SurfaceBuilder;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.gui.IGuiLabel;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;
import net.minecraft.client.renderer.GlStateManager;

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
        GlStateManager.color(1.F, 0.F, 0.F, 50 / 255.f);
        event.getSurfaceBuilder()
                .task(SurfaceBuilder::preRenderTexture2D)
                .task(SurfaceBuilder::preBlend)
                .beginQuads()
                .rectangle(getX(), getY(), getWidth(), getHeight())
                .end()
                .task(SurfaceBuilder::postRenderTexture2D)
                .task(SurfaceBuilder::postBlend);

        event.getSurfaceBuilder()
                .task(SurfaceBuilder::preFontRender)
                .task(SurfaceBuilder::preBlend)
                .fontRenderer(getFontRenderer())
                .color(getFontColor())
                .text(getText(), getX(), getY())
                .task(SurfaceBuilder::postFontRender)
                .task(SurfaceBuilder::postBlend);
    }
}
