package com.matt.forgehax.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

/**
 * com.matt.forgehax.gui
 * <p/>
 * Created on 7/8/2016 by Matthew
 */
public class GuiBlockList extends GuiScrollingList {
    public GuiBlockList(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight) {
        super(client,
                width,
                height,
                top,
                bottom,
                left,
                entryHeight,
                screenWidth,
                screenHeight);
    }

    @Override
    protected int getSize() {
        return 0;
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {

    }

    @Override
    protected boolean isSelected(int index) {
        return false;
    }

    @Override
    protected void drawBackground() {

    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {

    }
}
